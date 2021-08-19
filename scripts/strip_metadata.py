import io
import sys
import re
import yaml
from yaml import dump

try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper


def strip_metadata(input_text):
    rBranchingNode = re.compile(r'\w*-.*:')
    rOntoNode = re.compile(r'\w*(OntologyNode:)\w*')
    rName = re.compile(r'\w*(name:)\w*')
    wmLines = []
    for line in input_text.split("\n"):
        if len(rBranchingNode.findall(line)) == 1 and len(rOntoNode.findall(line)) == 0:
            wmLines.append(line)
        if len(rName.findall(line)) == 1:
            line = re.sub(r'( ){2}name: ', '- ', line)
            wmLines.append(line)
    return wmLines


def main():
    if len(sys.argv) != 3:
        print("usage: python strip_metadata.py [path_to_metadata_yaml] [output_file]")
    else:
        with open(sys.argv[1], "r") as ua:
            ua_yaml = yaml.load(ua, Loader=Loader)
            input_text = dump(ua_yaml, Dumper=Dumper)  # type=str
        with open(sys.argv[2], "w") as wm:
            for line in strip_metadata(input_text):
                wm.write(line + "\n")
        print("Successfully converted to stripped (human-diffable) format!")


if __name__ == '__main__':
    main()
