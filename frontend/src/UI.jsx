import React from 'react';
import logo from './pyneapple-logo-9.png';

function UI({
  selectedFile,
  handleChange,
  files,
  fetchData,
  apiParams,
  handleApiParamChange
}) {
  return (
    <div className="App">
      <header style={{ display: 'flex', alignItems: 'center' }}>
        <img src={logo} alt="Logo" style={{ width: '200px', height: '200px', marginRight: '20px' }} />
        <h1 style={{ fontSize: '24px' }}>Pyneapple App Demo</h1>
      </header>
      <div style={{ display: 'flex' }}>
        <div style={{ width: '15%', fontSize: '14px' }}>
          {/* Select File */}
          <div style={{ marginBottom: '10px' }}>
            <label htmlFor="selectedFile">Select File:</label>
            <select
              id="selectedFile"
              value={selectedFile}
              onChange={handleChange}
              style={{ fontSize: '14px', padding: '5px' }}
            >
              {files.map((file, index) => (
                <option value={file} key={index}>
                  {file}
                </option>
              ))}
            </select>
          </div>
          {/* Fetch Data Button */}
          <div style={{ marginBottom: '10px' }}>
            <button onClick={fetchData} style={{ fontSize: '14px', padding: '5px 10px' }}>
              Fetch Data
            </button>
          </div>
          {/* Parameter inputs */}
          <div>
            <label htmlFor="minName">minName:</label>
            <input
              type="text"
              id="minName"
              name="minName"
              value={apiParams.minName}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="minLow">minLow:</label>
            <input
              type="number"
              id="minLow"
              name="minLow"
              value={apiParams.minLow}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="minHigh">minHigh:</label>
            <input
              type="number"
              id="minHigh"
              name="minHigh"
              value={apiParams.minHigh}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="avgName">avgName:</label>
            <input
              type="text"
              id="avgName"
              name="avgName"
              value={apiParams.avgName}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="avgLow">avgLow:</label>
            <input
              type="number"
              id="avgLow"
              name="avgLow"
              value={apiParams.avgLow}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="avgHigh">avgHigh:</label>
            <input
              type="number"
              id="avgHigh"
              name="avgHigh"
              value={apiParams.avgHigh}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="sumName">sumName:</label>
            <input
              type="text"
              id="sumName"
              name="sumName"
              value={apiParams.sumName}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="sumLow">sumLow:</label>
            <input
              type="number"
              id="sumLow"
              name="sumLow"
              value={apiParams.sumLow}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="sumHigh">sumHigh:</label>
            <input
              type="number"
              id="sumHigh"
              name="sumHigh"
              value={apiParams.sumHigh}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="countLow">countLow:</label>
            <input
              type="number"
              id="countLow"
              name="countLow"
              value={apiParams.countLow}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="countHigh">countHigh:</label>
            <input
              type="number"
              id="countHigh"
              name="countHigh"
              value={apiParams.countHigh}
              onChange={handleApiParamChange}
              style={{ fontSize: '14px', padding: '5px' }}
            />
          </div>
        </div>
        {/* Map */}
        <div style={{ width: '85%', height: '600px', flexGrow: 1 }}>
          <div id="mapid" style={{ height: '100%', width: '100%' }}></div>
        </div>
      </div>
    </div>
  );
}

export default UI;
