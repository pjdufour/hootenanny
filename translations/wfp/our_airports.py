#!/bin/python

def translateAttributes(attrs, layerName, geometryType):
    if not attrs:
        return

    tags = {}

    tags['name'] = attrs.get('name')

    if attrs.get('type') == 'heliport':
        tags['aeroway'] = 'heliport'
    elif attrs.get('type') == "seaplane_base":
        tags['aeroway'] = 'aerodrome'
    elif attrs.get("type") == "closed":
        tags['aeroway'] = 'aerodrome'
    elif attrs.get("type") == "medium_airport":
        tags['aeroway'] = 'aerodrome'
    elif attrs.get("type") == "large_airport":
        tags['aeroway'] = 'aerodrome'
    else:
        tags['aeroway'] = 'aerodrome'

    tags['iata'] = attrs.get('iata_code', None)
    tags['wikipedia'] = attrs.get('wikipedia_link', None) or attrs.get("wikipedia_", None)
    tags['wfp:elevf'] = attrs.get('elevation_ft', None) or attrs.get("elevation_", None)

    # C++ cannot accept None values in dict, so need to remove them before return
    # https://docs.python.org/2/c-api/object.html#c.PyObject_CallObject
    tags = {k: v for k,v in tags.iteritems() if v is not None}
    print tags

    return tags
