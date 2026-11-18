"""Validate OpenAPI syntax and resolve the schema/response files."""
import json
from pathlib import Path

from openapi_spec_validator import OpenAPIV30SpecValidator


def main():
    root = Path(__file__).resolve().parents[1]
    path = root / 'src/main/webapp/swagger/openapi.json'
    specification = json.loads(path.read_text(encoding='utf-8'))
    OpenAPIV30SpecValidator(specification, base_uri=path.as_uri()).validate()
    print('OpenAPI validation passed.')


if __name__ == '__main__':
    main()
