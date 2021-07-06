import io
import sys
import re


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
			for line in ua:
				if len(rBranchingNode.findall(line)) == 1 and len(rOntoNode.findall(line)) == 0:
					wmLines.append(line)
				if len(rName.findall(line)) == 1:
					line = re.sub(r'( ){2}name: ', '- ', line)
					wmLines.append(line)
		if not error:
			with io.open(sys.argv[2], "w") as wm:
				for line in wmLines:
					wm.write(line)
			print("Successuflly converted to stripped (human-diffable) format!")


if __name__ == '__main__':
	main()
