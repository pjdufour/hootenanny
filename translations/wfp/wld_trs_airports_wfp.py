#!/bin/python

def translateAttributes(attrs, layerName, geometryType):
    if not attrs:
        return

    tags = {}
    tags['aeroway'] = 'aerodrome'

    tags['name'] = attrs.get('namelong') or attrs.get('nameshort') or attrs.get('namealt') or None
    tags['wfp:namelong'] = attrs.get('namelong')
    tags['wfp:nameshort'] = attrs.get('nameshort')
    tags['wfp:namealt'] = attrs.get('namealt')
    tags['icao'] = attrs.get('icao', None)
    tags['iata'] = attrs.get('iata', None)
    tags['elev'] = attrs.get('elevm', None)
    tags['wfp:elevm'] = attrs.get('elevm', None)

    # C++ cannot accept None values in dict, so need to remove them before return
    # https://docs.python.org/2/c-api/object.html#c.PyObject_CallObject
    tags = {k: v for k,v in tags.iteritems() if v is not None}
    print tags

    return tags


def translateToOgr(tags, elementType, geometryType):

    if elementType != 'Node':
        return None

    attrs = {
        "namelong": tags.get('wfp:namelong') or tags.get("name") or tags.get("name:en") or tags.get("int_name"),
        "nameshort": tags.get('wfp:nameshort') or tags.get("name") or tags.get("name:en") or tags.get("int_name"),
        "namealt": tags.get('wfp:namealt') or tags.get("name") or tags.get("name:en") or tags.get("int_name"),
        "icao": tags.get('icao'),
        "iata": tags.get('iata'),
        "elevm": tags.get('wfp:elevm') or tags.get('ele'),
        "elevf": tags.get('wfp:elevf')
        #"city": tags.get('city'),
        #"dtatus": tags.get('lata'),
        #"dmg": tags.get('dmg'),
        #"wwpaved": tags.get('wwpaved'),
        #"apttype": tags.get('apttype'),
        #"aptclass": tags.get('aptclass'),
        #"authority": tags.get('authority'),
        #"rwlengthm": tags.get('rwlengthm'),
        #"rwlengthf": tags.get('rwlengthf'),
        #"elevf": tags.get('elevf'),
        #"humuse": tags.get('humuse'),
        #"humoperatedby": tags.get('humoperatedby'),
        #"latitude": tags.get('latitude'),
        #"iso3": tags.get('iso3'),
        #"lastcheckdate": tags.get('lastcheckdate'),
        #"remarks": tags.get('remarks'),
        #"url_lca": tags.get('url_lca'),
        #"createdate": tags.get('createdate'),
        #"updatedate": tags.get('updatedate'),
        #"locprecision": tags.get('locprecision'),
        #"longitude": tags.get('longitude'),
        #"iso3_op": tags.get('iso3_op'),
        #"country": tags.get('country'),
        #"source": tags.get('source'),
        #"geonameid": tags.get('geonameid')
    }

    attrs = {k: v for k,v in attrs.iteritems() if v is not None}

    return { attrs: attrs, tableName: "wld_trs_airports_wfp" }

def getDbSchema():

    columns = [
        {"name": "namelong", "type": "String", "defValue": ""},
        {"name": "nameshort", "type": "String", "defValue": ""},
        {"name": "namealt", "type": "String", "defValue": ""},
        {"name": "icao", "type": "String", "defValue": ""},
        {"name": "iata", "type": "String", "defValue": ""},
        {"name": "elevm", "type": "String", "defValue": ""},
        {"name": "elevf", "type": "String", "defValue": ""}
    ]

    schema = [
        {
            name: 'wld_trs_airports_wfp',
            geom: 'Point',
            columns: columns
        }
    ]

    return schema
