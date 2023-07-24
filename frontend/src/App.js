import React, { useState, useEffect } from 'react';
import axios from 'axios';
import L from 'leaflet';
import * as shapefile from 'shapefile';
import UI from './UI';

function App() {
  const [geoLayer, setGeoLayer] = useState(null);
  const [map, setMap] = useState(null);
  const [selectedFile, setSelectedFile] = useState('LACity');
  const [files, setFiles] = useState([]);
  const [apiParams, setApiParams] = useState({
    file_name: `${selectedFile}.shp`,
    disname: 'households',
    minName: 'pop_16up',
    minLow: 0,
    minHigh: 3000,
    maxName: 'pop_16up',
    maxLow: 0,
    maxHigh: 99999,
    avgName: 'employed',
    avgLow: 1000,
    avgHigh: 4000,
    sumName: 'pop2010',
    sumLow: 200000,
    sumHigh: 999999,
    countLow: -999999,
    countHigh: 999999,
  });

  useEffect(() => {
    if (selectedFile) {
      let fileName = selectedFile.endsWith('.shp') ? selectedFile : `${selectedFile}.shp`;
      setApiParams((prevParams) => ({
        ...prevParams,
        file_name: fileName,
      }));
    }
  }, [selectedFile]);

  useEffect(() => {
    axios
      .get('http://localhost:8000/listFiles')
      .then((response) => {
        setFiles(response.data);
      })
      .catch((error) => {
        console.log('Error fetching files:', error);
      });
  }, []);

  useEffect(() => {
    if (!map) {
      const mymap = L.map('mapid').setView([34.0522, -118.2437], 10);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
      }).addTo(mymap);
      setMap(mymap);
    }
  }, [map]);

  useEffect(() => {
    if (map && selectedFile) {
      if (geoLayer) {
        map.removeLayer(geoLayer);
      }
      shapefile
        .read(`http://localhost:8003/test_react/data/${selectedFile}`)
        .then(({ features }) => {
          const newGeoLayer = L.geoJSON(features, {
            style: (feature) => {
              return {
                fillColor: getColor(feature.properties.POP),
                color: '#000',
                fillOpacity: 0.5,
              };
            },
            onEachFeature: (feature, layer) => {
              layer.on('click', function () {
                if (geoLayer) {
                  geoLayer.setStyle({ fillOpacity: 0.5 });
                  layer.setStyle({ fillOpacity: 1 });
                }
              });

              const tooltipContent = `Tract: ${feature.properties.TRACTCE10}\nTotal Population: ${feature.properties.POP}`;
              layer.bindTooltip(tooltipContent).openTooltip();
            },
          }).addTo(map);

          setGeoLayer(newGeoLayer);
        })
        .catch((error) => {
          console.log('Error loading shapefile:', error);
        });
    }
  }, [map, selectedFile]);

  function fetchData() {
    axios
      .get('http://localhost:8000/api/endpoint', {
        params: apiParams,
      })
      .then((response) => {
        const labels = response.data.labels;

        // Create a mapping from labels to colors
        const labelColorMap = {};
        labels.forEach((label) => {
          if (!labelColorMap[label]) {
            labelColorMap[label] = getRandomColor();
          }
        });

        setGeoLayer((currentGeoLayer) => {
          let labelIndex = 0;
          currentGeoLayer.eachLayer(function (layer) {
            const label = labels[labelIndex];
            const color = labelColorMap[label];
            layer.setStyle({ fillColor: color });
            layer.bindTooltip(`${label}`);
            labelIndex += 1;
          });

          // Update legend
          const legend = L.control({ position: 'bottomright' });
          legend.onAdd = function (map) {
            const div = L.DomUtil.create('div', 'info legend');
            div.innerHTML += '<h4>Data</h4>';
            for (let label in labelColorMap) {
              div.innerHTML +=
                '<i style="background:' +
                labelColorMap[label] +
                '"></i> ' +
                label +
                '<br>';
            }
            return div;
          };
          legend.addTo(map);
          return currentGeoLayer;
        });
      })
      .catch((error) => {
        console.log('Error fetching data:', error);
      });
  }

  const handleChange = (event) => {
    setSelectedFile(event.target.value);
  };

  const handleApiParamChange = (event) => {
    const { name, value } = event.target;
    setApiParams((prevParams) => ({
      ...prevParams,
      [name]: value,
    }));
  };

  const getColor = (value) => {
    return '#000';
  };

  const getRandomColor = () => {
    let color = '#';
    const letters = '0123456789ABCDEF';
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  };

  return (
    <UI
      selectedFile={selectedFile}
      handleChange={handleChange}
      files={files}
      fetchData={fetchData}
      apiParams={apiParams}
      handleApiParamChange={handleApiParamChange}
    />
  );
}

export default App;
