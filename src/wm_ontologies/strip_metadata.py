import io
import sys
import re

def main():
	if len(sys.argv) != 3:
		print("usage: python -m wm_ontologies.strip_metadata.py [path_to_metadata_yaml] [output_file]")
	else:
		ontoNode = ""
		rOntoNode = re.compile(r'\w*(OntologyNode:)\w*')
		name = ""
		rName = re.compile(r'\w*(name:)\w*')
		# todo: script assumes that the OntNode metadata will end with `polarity` -- we should adjust this if 
		#       we change format!
		polarity = False
		rPolarity = re.compile(r'\w*(semantic type:)\w*')
		wrong_line = 0
		wmLines = []
		error = False
		with io.open(sys.argv[1], "r") as ua:
			for line in ua:
				wm = line
				if ontoNode != "" and len(rName.findall(line)) == 0 and len(rPolarity.findall(line)) == 0:
					wrong_line += 1
					continue
				if len(rOntoNode.findall(line)) == 1:
					ontoNode = line
					wrong_line += 1
					continue
				if len(rName.findall(line)) == 1:
					name = line
					wrong_line += 1
					continue
				if len(rPolarity.findall(line)) == 1:
					if ontoNode == "" or name == "":
						error = True
						print("Structure error at line " + str(wrong_line + 1))
						break
					try:
						wm = ontoNode[0:ontoNode.index("O")] + name[name.index(":")+2:]
						ontoNode = ""
						name = ""
					except:
						print("Structure error at line " + str(wrong_line + 1))
						error = True
						break
				wrong_line += 1
				wmLines.append(wm)
		if not error:
			with io.open(sys.argv[2], "w") as wm:
				for line in wmLines:
					wm.write(line)
			print("Successuflly converted to stripped (human-diffable) format!")

if __name__ == '__main__':
	main()