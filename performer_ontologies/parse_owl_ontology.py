import os
import sys
import codecs
import re
from collections import defaultdict

# run this python script to produce ../kimetrica_intervention.with_objects.txt

owl_file = 'kimetrica_root_ontology.112419.owl'


lines = []
with codecs.open(owl_file, 'r', encoding='utf-8') as f:
  for line in f:
    lines.append(line.strip())


name_to_concept = dict()

class OntologyConcept(object):
  def __init__(self, name):
    self.name = name
    self.parents = set()
    self.children = set()
    self.related_objects = set()



i = 0
while(i < len(lines)):
  if '<owl:Class ' in lines[i]:
    node1 = re.search(r'^(.*)"(.*)#(.*?)"', lines[i]).group(3)
    if node1 not in name_to_concept:
      name_to_concept[node1] = OntologyConcept(node1)

    if lines[i].endswith('/>'):
      i += 1
      continue

    i += 1
    while lines[i] != '</owl:Class>':
      if '<rdfs:subClassOf ' in lines[i]:
        node2 = re.search(r'^(.*)"(.*)#(.*?)"', lines[i]).group(3)
        if node2 not in name_to_concept:
          name_to_concept[node2] = OntologyConcept(node2)
        name_to_concept[node1].parents.add(node2)
        name_to_concept[node2].children.add(node1)

      elif '<owl:onProperty ' in lines[i] and '#relatedObject"' in lines[i]:
        i += 1
        if '<owl:someValuesFrom ' in lines[i]:
          node3 = re.search(r'^(.*)"(.*)#(.*?)"', lines[i]).group(3)
          if node3 not in name_to_concept:
            name_to_concept[node3] = OntologyConcept(node3)
          name_to_concept[node1].related_objects.add(node3)

      i += 1

  i += 1


root_names = set()
for name in name_to_concept:
  c = name_to_concept[name]
  if len(c.parents) == 0:
    root_names.add(name)

def print_children(name, prefix, names_on_branch):
  """
  :type name: str
  :type prefix: str
  :type names_on_branch: set[str]
  """
  print('{}{}'.format(prefix, name))
  print('{}#{}'.format(prefix+'  ', ','.join(name_to_concept[name].related_objects)))
  for c in name_to_concept[name].children:
    if c not in names_on_branch:	# to prevent loops
      print_children(c, prefix+'   ', names_on_branch+[c])

  
for name in root_names:
  print_children(name, '', [name])


