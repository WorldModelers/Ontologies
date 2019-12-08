"""
This code takes an RDF file (eg:from kimetrica) and converts it into a YAML ontology suitable for usage with Eidos.

Prerequisites:
    python3, rdfextras,rdflib
Usage:
     python convert_owl_yaml.py  --input_file [INPUT_FILE] --output_file [OUTPUT_FILE]
Default Behavior:
    python convert_owl_yaml.py

    (expects input file named as root-ontology.owl  kept at the same level as
    this script and the output file will be created as interventions_metadata.yml)

"""


import rdflib
import yaml
import argparse
import sys



def create_parser():
    parser = argparse.ArgumentParser(description='Arguments for converting owl to yaml')
    parser.add_argument('--input_file', type=str, default='root-ontology.owl',
                        help='the owl file which has to be converted to yaml')
    parser.add_argument('--output_file', type=str, default='interventions_metadata.yml',
                        help='name of the converted yaml file')

    return parser


def parse_commandline_args():
    return create_parser().parse_args()

def check_add_to_dict(dict_to_check, key,val):
    if key in dict_to_check:
        current_val=dict_to_check[key]
        current_val.append(val)
        dict_to_check[key]=current_val
    else:
        current_val=[]
        current_val.append(val)
        dict_to_check[key] = current_val


def get_parent_child_sparql(g):
    child_parent_dict = {}
    parent_child_dict={}
    res = g.query("""SELECT DISTINCT ?child_label ?parent_label
    WHERE {
    ?child_iri rdfs:subClassOf ?parent_iri .
    ?child_iri rdfs:label ?child_label .
    ?parent_iri rdfs:label ?parent_label
    }""")
    for child,parent in res:
        check_add_to_dict(child_parent_dict,str(child),str(parent))
        check_add_to_dict(parent_child_dict, str(parent), str(child))
    return child_parent_dict,parent_child_dict

def get_obj_event_appliedTo_sparql(g,child_parent_dict):
    event_obj_for_appliedTo={}

    #read comments from bottom up to understand the query
    res = g.query("""SELECT DISTINCT ?events_label ?objects_label
    WHERE {
    #for each of the nodes which have label as iri, make sure it has the iri of appliedTo as onProperty 
        ?restrictions_iri owl:onProperty ?property_iri .
    #ide test
        ?restrictions_iri rdf:type owl:Restriction .
        ?restrictions_iri owl:someValuesFrom ?object_iri .
        ?object_iri rdfs:label ?objects_label .
        ?property_iri rdf:type owl:ObjectProperty .
    #get the unique identifier( iri) of all the nodes which have the label as appliedTo  eg:Na2ebb1c6a3b445aca99f9c4fca7c7dff
        ?property_iri rdfs:label "AppliedTo"^^xsd:string .
        ?events_iri rdfs:subClassOf ?restrictions_iri .
        ?events_iri rdfs:label ?events_label .
    }""")

    for event, object in res:
        ancestry_tree = get_ancestry_tree(child_parent_dict, str(object))
        check_add_to_dict(event_obj_for_appliedTo,str(event),str(ancestry_tree))
    return event_obj_for_appliedTo

# Since we seem to want a None for OntologyNode, but don't want to display it :)
def represent_none(self, _):
    return self.represent_scalar('tag:yaml.org,2002:null', '')


def ont_node(name, examples, keywords,appliesTo, add_name = True):
    # If selected, make sure the node name is added to the examples to be used for grounding
    if add_name:
        examples.append(name)
    if len(appliesTo)>0:
        d = {'OntologyNode': None,"name": name,'examples': examples, 'appliedTo':appliesTo,'polarity': 1}
    else:
        d = {'OntologyNode': None, "name": name,'examples': examples,  'polarity': 1}
    if keywords is not None:
        d['keywords'] = keywords
    return d

#given any child/leaf node, get the complete path of it starting from the topmost parent of it
def get_ancestry_tree(child_parent_dict, label):
    if label not in child_parent_dict:
        return label
    for c in child_parent_dict[label]:
        n = get_ancestry_tree(child_parent_dict, c) + "/" + label
    return n


def make_hierarchy(parent_child_dict, label,child_parent_dict,event_obj_for_appliedTo):
    # if the label doesn't exist in a parent_child_dict it means its a leaf
    if label not in parent_child_dict:
        if label in event_obj_for_appliedTo:
            obj_this_event_applies_to=event_obj_for_appliedTo[label]
            return ont_node(label,[],None,obj_this_event_applies_to)
        else:
            return ont_node(label, [], None, [])
    node = {label:[]}
    for c in parent_child_dict[label]:
        n = make_hierarchy(parent_child_dict, c,child_parent_dict,event_obj_for_appliedTo)
        node[label].append(n)
    return node

def dump_yaml(data, fn):
    with open(fn, 'w') as yaml_file:
        yaml.dump(data, yaml_file, default_flow_style=False, sort_keys=False)



def main():
    if len(sys.argv) != 5:
        print("usage: python convert_owl_yaml.py  --input_file [INPUT_FILE] --output_file [OUTPUT_FILE]")
    else:
        args = parse_commandline_args()
        yaml.add_representer(type(None), represent_none)
        g = rdflib.Graph()
        g.load(args.input_file)
        child_parent_dict, parent_child_dict=get_parent_child_sparql(g)
        event_obj_for_appliedTo = get_obj_event_appliedTo_sparql(g, child_parent_dict)
        data = [{"Interventions":[make_hierarchy(parent_child_dict, 'Events',child_parent_dict,event_obj_for_appliedTo),
            make_hierarchy(parent_child_dict, 'Objects',child_parent_dict,event_obj_for_appliedTo),
            make_hierarchy(parent_child_dict, 'Organizations',child_parent_dict,event_obj_for_appliedTo),]}]
        dump_yaml(data, args.output_file)

if __name__ == '__main__':
    main()