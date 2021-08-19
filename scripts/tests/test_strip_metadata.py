from strip_metadata import strip_metadata


class TestStripMetadata:
    def test1(self):
        metadataful = """- wm:
  - concept:
    - agriculture:
      - OntologyNode:
        examples:
        - additives
        - amounts
        - balanced feed
        name: animal_feed
        polarity: 1
        semantic type: entity
      - OntologyNode:
        examples:
        - animal science
        - veterinary science
        name: animal_science
        polarity: 1
        semantic type: event"""

        metadataless = """- wm:
  - concept:
    - agriculture:
      - animal_feed
      - animal_science"""

        output = "\n".join(strip_metadata(metadataful))
        assert output == metadataless
