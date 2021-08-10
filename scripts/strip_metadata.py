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
        print("usage: python strip_metadata.py [path_to_metadata_yaml] [output_file]")
    else:
        rBranchingNode = re.compile(r'\w*-.*:')
        rOntoNode = re.compile(r'\w*(OntologyNode:)\w*')
        rName = re.compile(r'\w*(name:)\w*')
        wmLines = []
        error = False
        with io.open(sys.argv[1], "r") as ua:
            ua_yaml = yaml.load(ua, Loader=Loader)
            yaml_arranged = dump(ua_yaml, Dumper=Dumper)  # type=str
            for line in yaml_arranged.split("\n"):
                if len(rBranchingNode.findall(line)) == 1 and len(rOntoNode.findall(line)) == 0:
                    wmLines.append(line)
                if len(rName.findall(line)) == 1:
                    line = re.sub(r'( ){2}name: ', '- ', line)
                    wmLines.append(line)
        if not error:
            with io.open(sys.argv[2], "w") as wm:
                for line in wmLines:
                    wm.write(line + "\n")
            print("Successfully converted to stripped (human-diffable) format!")


if __name__ == '__main__':
    main()
