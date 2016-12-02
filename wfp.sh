#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# sudo dd if=/dev/zero of=/swapfile bs=1024 count=8388608
############# Environment #############
echo "Setting up environment"
DATA=/home/vagrant/hoot/data
TRANS_OGR_TO_OSM=/home/vagrant/hoot/translations/wfp/wld_trs_airports_wfp.py
TRANS_OSM_TO_OGR=/home/vagrant/hoot/translations/wfp/wld_trs_airports_wfp.js

REF_INPUT=$DATA/input/shapefile/wld_trs_airports_wfp.shp
REF_OSM_PBF=$DATA/intermediate/osm/wld_trs_airports_wfp.osm.pbf

OUR_AIRPORTS_INPUT=$DATA/input/csv/our_airports.csv
OUR_AIRPORTS_VRT=$DATA/input/csv/our_airports.vrt
OUR_AIRPORTS_SHP=$DATA/input/csv/our_airports.shp
OUR_AIRPORTS_OSM_PBF=$DATA/intermediate/osm/our_airports.osm.pbf
OUR_AIRPORTS_TRANS_OGR_TO_OSM=/home/vagrant/hoot/translations/wfp/our_airports.py

ALL_OSM_PBF=$DATA/intermediate/all.osm.pbf
CONFLATED_OSM_PBF=$DATA/conflated/wld_trs_airports_wfp.osm.pbf
CONFLATED_SHP=$DATA/conflated/wld_trs_airports_wfp.shp

############# Clean Data #############
echo "Cleaning Data"
# Don't need to delete intermediate data unless original files have changed.
#!!!!!rm -fr $DATA/intermediate
rm -fr $DATA/conflated

#hadoop rmr wld_trs_airports_wfp.osm.pbf
############# WFP Data #############
echo "Preparing WFP Reference Data"
if [ ! -f $REF_OSM_PBF ]; then
  hoot ogr2osm $TRANS_OGR_TO_OSM $REF_OSM_PBF $REF_INPUT
fi
#hadoop rmr wld_trs_airports_wfp.osm.pbf
#hadoop fs -mkdir wld_trs_airports_wfp.osm.pbf
#hadoop fs -put $DATA/osm/wld_trs_airports_wfp.osm.pbf wld_trs_airports_wfp.osm.pbf/Input.osm.pbf

############# Our Airports Data #############
echo "Preparing Our Airports Data"
mkdir -p $DATA/intermediate/shapefile
if [ ! -f $OUR_AIRPORTS_SHP ]; then
  cd $DIR/data/input/csv
  ogr2ogr -f "ESRI Shapefile" -nlt POINT our_airports.shp our_airports.vrt
  cd $DIR
fi

if [ ! -f $OUR_AIRPORTS_OSM_PBF ]; then
  hoot ogr2osm $OUR_AIRPORTS_TRANS_OGR_TO_OSM $OUR_AIRPORTS_OSM_PBF $OUR_AIRPORTS_SHP
fi

############# OSM Data #############
echo "Preparing OSM Data"

#osmosis \
#  --read-pbf $DATA/osm/ghana-latest.osm.pbf \
#  --tf accept-nodes aeroway=* \
#  --tf accept-ways aeroway=* \
#  --tf accept-relations aeroway=* \
#  --used-node \
#  --write-xml $DATA/osm/ghana_airports_all.osm

mkdir -p $DATA"/intermediate/osm"

#for f in $DATA/input/osm/*
#do
#  f_ext=${f: -4}
#  f_name=${f##*/}
#  f_name=${f_name%.osm.pbf}
#  echo "Preparing OSM Data from $f"
#  echo "f_ext: $f_ext"
#  echo "f_name: $f_name"
#  osmconvert $f -o=$DATA/intermediate/osm/$f_name".osm"
#  osmfilter $DATA/intermediate/osm/$f_name".osm" --keep="aeroway=" -o=$f_name"_aeroways.osm"
#  osmconvert $DATA/intermediate/osm/$f_name"_aeroways.osm" --add-bbox-tags --all-to-nodes -o=$DATA/intermediate/osm/$f_name"_aeroways_nodes.osm"
#  osmfilter $DATA/intermediate/osm/$f_name"_aeroways_nodes.osm" --keep="aeroway=" -o=$DATA/intermediate/osm/$f_name"_aeroways_cleaned.osm"
#  osmconvert $DATA/intermediate/osm/$f_name"_aeroways_cleaned.osm" -o=$DATA/intermediate/osm/$f_name"_aeroways_cleaned.osm.pbf"
#done

echo "Unpacking .osm.pbf files into regular .osm xml"
for f in $DATA/input/osm/*
do
  f_ext=${f: -4}
  f_name=${f##*/}
  f_name=${f_name%.osm.pbf}
  of=$DATA/intermediate/osm/$f_name".osm"
  if [ ! -f $of ]; then
    osmconvert $f -o=$of
  fi
done

echo "Filtering to just aeroway=* and converting to just nodes"
for f in $DATA/input/osm/*
do
  f_ext=${f: -4}
  f_name=${f##*/}
  f_name=${f_name%.osm.pbf}

  of=$DATA/intermediate/osm/$f_name"_aeroways.osm"
  if [ ! -f $of ]; then
    osmfilter $DATA/intermediate/osm/$f_name".osm" --keep="aeroway=" -o=$of
  fi

  of=$DATA/intermediate/osm/$f_name"_aeroways_nodes.osm"
  if [ ! -f $of ]; then
    osmconvert $DATA/intermediate/osm/$f_name"_aeroways.osm" --add-bbox-tags --all-to-nodes -o=$of
  fi

  of=$DATA/intermediate/osm/$f_name"_aeroways_cleaned.osm"
  if [ ! -f $of ]; then
    osmfilter $DATA/intermediate/osm/$f_name"_aeroways_nodes.osm" --keep="aeroway=" -o=$of
  fi
done

echo "Packing *.osm into *.osm.pbf"
for f in $DATA/input/osm/*
do
  f_ext=${f: -4}
  f_name=${f##*/}
  f_name=${f_name%.osm.pbf}
  of=$DATA/intermediate/osm/$f_name"_aeroways_cleaned.osm.pbf"
  if [ ! -f $of ]; then
    osmconvert $DATA/intermediate/osm/$f_name"_aeroways_cleaned.osm" -o=$of
  fi
done

#hadoop fs -rmr ghana_airports.osm.pbf
#hadoop fs -mkdir ghana_airports.osm.pbf
#hadoop fs -put $DATA/osm/ghana_airports_final.osm.pbf ghana_airports.osm.pbf/Input.osm.pbf

#hadoop fs -rmr conflated.osm.pbf
#hadoop fs -mkdir conflated.osm.pbf
#hoot big-conflate wld_trs_airports_wfp.osm.pbf/Input.osm.pbf ghana_airports.osm.pbf/Input.osm.pbf conflated.osm.pbf/Output.osm.pbf 0.001
############# Conflation #############
mkdir -p $DATA/conflated
echo "Conflating Data"
CONFLATION_INPUT_FILES="$OUR_AIRPORTS_OSM_PBF "
for f in $DATA/input/osm/*
do
  f_ext=${f: -4}
  f_name=${f##*/}
  f_name=${f_name%.osm.pbf}
  CONFLATION_INPUT_FILES=$CONFLATION_INPUT_FILES" "$DATA/intermediate/osm/$f_name"_aeroways_cleaned.osm.pbf"
done

echo "Merging all input files together"
rm -f $ALL_OSM_PBF
hoot big-cat $ALL_OSM_PBF $CONFLATION_INPUT_FILES

rm -f $CONFLATED_OSM_PBF
hoot conflate $ALL_OSM_PBF $CONFLATED_OSM_PBF

############# Export #############
echo "Exporting to OGR (Shapefile)"
rm -f $CONFLATED_SHP
hoot osm2ogr $TRANS_OSM_TO_OGR $CONFLATED_OSM_PBF $CONFLATED_SHP
