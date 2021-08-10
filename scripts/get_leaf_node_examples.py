import io
import sys
import re
import yaml
from yaml import dump

try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper


def main():
    if len(sys.argv) != 3:
        print("usage: python get_leaf_node_examples.py [path_to_metadata_yaml] [output_file]")
    else:
        rBranchingNodes = re.compile(r'-.*:\n')
        rMetadata = r'\n((- OntologyNode:.*)|(polarity:.*)|(semantic type:.*)|(definition:.*))'
        rPatterns = r'pattern:\n(- .*\n)+'
        with io.open(sys.argv[1], "r") as onto:
            ontoLines = yaml.load(onto, Loader=Loader)
            ontoLines = dump(ontoLines, Dumper=Dumper)
            ontoLines = re.sub(r'\n\s+', '\n', ontoLines)  # removes extra spaces
            ontoLines = re.sub(rMetadata, '', ontoLines)  # removes (most) metadata fields
            ontoLines = re.sub(rBranchingNodes, '', ontoLines)  # removes non-leaf nodes
            ontoLines = re.sub(rPatterns, '', ontoLines)  # removes pattern metadata field w/ patterns
            ontoLines = re.sub(r'(examples:\n(-.*\n)+)(name:.*\n)', r'\3\1', ontoLines)  # swaps name and example order

        with io.open(sys.argv[2], "w") as wm:
            wm.write(ontoLines)
        print("Successfully created list of leaf nodes with their examples!")


if __name__ == '__main__':
    main()
