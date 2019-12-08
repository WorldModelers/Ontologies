import re
import sys
import yaml

from convert_owl_yaml import ont_node, represent_none


fn = sys.argv[1]

y = yaml.load(open(fn))

def node_dict_to_node(d):
    print(d.keys())
    related_event = d.get('relatedEvent', None)
    # print(related_event)
    aliases = d.get('aliases', [])
    # print("aliases:", aliases)
    inner = d['OntologyNode']
    examples = inner['examples']
    # print("examples:", examples)

    name = inner['name'].lower()
    name = re.sub(" ", "_", name)
    # print("name:", name)
    polarity = int(inner['polarity'])
    # print("polarity:", polarity)
    examples = [re.sub("_", " ", x) for x in examples]
    new_examples = [ex.lower().strip() for ex in examples + aliases]
    if related_event:
        new_examples.append(related_event.lower())
    # print("new_examples:", new_examples)
    # name, examples, keywords,appliesTo, add_name = True
    node = ont_node(name, new_examples, None, [], False)
    print(node)
    return node

def dump_yaml(data, fn):
    with open(fn, 'w') as yaml_file:
        yaml.dump(data, yaml_file, default_flow_style=False)



def main():
    yaml.add_representer(type(None), represent_none)
    # get the intervention types at the end, for the flat ontology, seems to already be flattened!
    # thanks Kimetrica!
    interventions = y[0]['Interventions'][2]['Intervention Types']
    branches = []
    for inner_d in interventions:
        for inter_type, interventions_of_type in inner_d.items():
            inter_type = inter_type.lower()
            inter_type = re.sub(" ", "_", inter_type)
            print()
            print(inter_type, len(interventions_of_type), type(interventions_of_type))
            nodes = []
            for d in interventions_of_type:
                nodes.append(node_dict_to_node(d))
            branch = {inter_type: nodes}
            branches.append(branch)

    final = [{'interventions': branches}]
    dump_yaml(final, 'flattened_interventions.yml')



if __name__ == '__main__':
    main()

