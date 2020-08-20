from collections import defaultdict
import yaml
import csv
import re
import sys

'''
    This is a script to convert the spreadsheet version of an ontology into the
    yaml-with-metadata format.
    Assumes the current format of sheet made by Alli for the compositional ontology
    (2020-08-20)
    
    Usage: 
        `python convert_csv_to_yaml.py <csv spreadsheet> <filename for yaml version>`
'''


# Since we seem to want a None for OntologyNode, but don't want to display it :)
def represent_none(self, _):
    return self.represent_scalar('tag:yaml.org,2002:null', '')

yaml.add_representer(type(None), represent_none)


def ont_node(name, examples, keywords, add_name = True):
    # If selected, make sure the node name is added to the examples to be used for grounding
    if add_name:
        examples.extend(name.split("_"))
    d = {'OntologyNode': None, "name": name, 'examples': examples, 'polarity': 1.0}
    if keywords is not None:
        d['keywords'] = keywords
    return d


def dump_yaml(d, fn):
    with open(fn, 'w') as yaml_file:
        yaml.dump(d, yaml_file, default_flow_style=False)

def get_all_nodes(csv_file):
    parent_to_child = defaultdict(list)
    child_to_examples = {}

    with open(csv_file, "r") as f:
        reader = csv.reader(f)
        next(reader, None)  # skip the headers
        for row in reader:
            examples = row[0]
            examples = re.sub("_", " ", examples)
            examples = [ex.strip() for ex in examples.split(',')]
            examples = [ex for ex in examples if ex is not '']

            category = row[3].strip()
            assert category in ['concept', 'entity', 'process', 'property', 'time', ''], f'[{category}] :: {row}'

            if category != '':
                path = [col.strip() for col in row[3:]]
                path = ['wm'] + [x for x in path if x != '']
                if len(path) > 1:
                    child = path[-1]
                    parent = path[-2]
                    parent_to_child[parent].append(child)
                    if child not in child_to_examples:
                        child_to_examples[child] = examples
    return parent_to_child, child_to_examples

def is_leaf(node, parent_to_child):
    # it's a leaf if it's not a parent
    return node not in parent_to_child

def nest_nodes(node, parent_to_child, child_to_examples):
    if is_leaf(node, parent_to_child):
        return ont_node(node, child_to_examples[node], None, add_name = True)
    else:
        children = parent_to_child[node]
        return {node: [nest_nodes(child, parent_to_child, child_to_examples) for child in children]}


def main():
    if len(sys.argv) != 3:
        print('ERROR: Two arguments are required.  \nUsage:')
        print('\t `python convert_csv_to_yaml.py <csv spreadsheet> <filename for yaml version>`')
        sys.exit(1)
    csv_file = sys.argv[1]
    ont_file = sys.argv[2]

    parent_to_child, child_to_examples = get_all_nodes(csv_file)
    # Wrap in a list bc this is what we've been doing
    nested = [nest_nodes("wm", parent_to_child, child_to_examples)]

    dump_yaml(nested, ont_file)

if __name__ == "__main__":
    main()