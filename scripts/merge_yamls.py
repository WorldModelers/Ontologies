import io
import sys
import re
import yaml
from yaml import dump

try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper

new_nodes = []

def main():
    if len(sys.argv) != 4:
        print("usage: python merge_yamls.py [path_to_input_file_1] [path_to_input_file_2] [output_file]")
    else:

        with open(sys.argv[1], 'r') as f1:
            yaml1 = yaml.safe_load(f1)[0]
        with open(sys.argv[2], 'r') as f2:
            yaml2 = yaml.safe_load(f2)[0]

        merged = merge(yaml1, yaml2)
        merged_yaml = dump(merged, Dumper=Dumper)
        merged_yaml = re.sub(r'\n', '\n  ', merged_yaml)
        merged_yaml = re.sub(r'wm:', '- wm:', merged_yaml)

        with io.open(sys.argv[3], "w") as wm:
            wm.write(merged_yaml)
        print("Successfully merged yaml files!")


def merge(d1, d2):
    new_branches = []
    for key in d2:
        if key in d1:
            node1_names = [node['name'] for node in d1[key] if len(node) != 1]
            node2_names = [node['name'] for node in d2[key] if len(node) != 1]
            branching_nodes1 = [node.keys() for node in d1[key] if len(node) == 1]
            branching_nodes2 = [node.keys() for node in d2[key] if len(node) == 1]
            for node1 in d1[key]:
                if len(node1) == 1:
                    for node2 in d2[key]:
                        if len(node2) == 1:
                            branching_name2 = node2.keys()
                            if list(branching_name2)[0] not in new_branches:
                                if branching_name2 in branching_nodes1:
                                    merge(node1, node2)
                                else:
                                    new_branches.append(list(branching_name2)[0])
                                    d1[key].append(node2)
                else:
                    name1 = node1['name']
                    if name1 not in new_nodes:
                        for node2 in d2[key]:
                            if len(node2) == 1:
                                continue
                            else:
                                name2 = node2['name']
                                if name2 not in node1_names and name2 not in new_nodes:
                                    new_nodes.append(name2)
                                    d1[key].append(node2)
                                    break
                                else:
                                    if name1 == name2:
                                        node1['examples'] = sorted(set(node1['examples'] + node2['examples']))
        else:
            continue

    return d1


for item in new_nodes:
    print(item)

if __name__ == '__main__':
    main()
