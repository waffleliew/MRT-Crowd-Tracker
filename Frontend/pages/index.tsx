import type { NextPage } from "next";
import { useEffect, useRef, useState } from "react";
import Map, { MapRef, Marker } from "react-map-gl/maplibre";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import { locations } from "../library/locations";

const API_URL = "http://localhost:8080"

const IndexPage: NextPage = () => {
  const mapRef = useRef<MapRef>(null);
  const [stationCrowdLevels, setStationCrowdLevels] = useState<{ [key: string]: string }>({});

    // // Fetch historical crowd density data from MongoDB on page load
    useEffect(() => {
      const fetchCrowdData = async () => {
        try {
          const response = await fetch(`${API_URL}/api/mrt-density`);
          const data = await response.json();
  
          // Transform API data into a state-friendly format
          const initialCrowdLevels: { [key: string]: string } = {};
          data.forEach((entry: { station: string; crowdLevel: string }) => {
            initialCrowdLevels[entry.station] = entry.crowdLevel;
          });
  
          setStationCrowdLevels(initialCrowdLevels);
          console.log("Initial crowd data fetched:", initialCrowdLevels);
        } catch (error) {
          console.error("Error fetching initial crowd data:", error);
        }
      };
  
      fetchCrowdData();
    }, []);
  

  // WebSocket Connection
  useEffect(() => {
    const socket = new SockJS(`${API_URL}/ws`); // WebSocket from Spring Boot
    const stompClient = Stomp.over(socket);
    

    stompClient.connect({}, () => {
      stompClient.subscribe("/topic/crowdDensity", (message) => {
        const jsonData = JSON.parse(message.body);
        const stationCode = jsonData.station; // Example: "TE7"
        const crowdLevel = jsonData.crowdLevel; // "h", "m", "l"

        console.log("Received data:", jsonData);

        // Update station crowd levels
        setStationCrowdLevels((prevData) => ({
          ...prevData,
          [stationCode]: crowdLevel, // Assign crowd level to station code
        }));
      });
    });

    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, []);

  // Function to get marker color based on crowd level
  const getMarkerColor = (stationCode: string) => {
    const crowdLevel = stationCrowdLevels[stationCode];
    switch (crowdLevel) {
      case "h":
        return "rgba(255, 0, 0, 0.7)"; // Red (High density)
      case "m":
        return "rgba(255, 165, 0, 0.7)"; // Amber (Medium density)
      case "l":
        return "rgba(0, 255, 0, 0.7)"; // Green (Low density)
      default:
        return "rgba(0, 0, 255, 0.5)"; // Blue (default if no data yet)
    }
  };

  // Zoom to location on click
  const flyTo = (coordinates: [number, number]): void => {
    const map = mapRef.current?.getMap();
    if (!map) return;

    map.flyTo({
      center: coordinates,
      essential: true,
      zoom: 14,
    });
  };

  return (
    <div style={{ position: "relative" }}>
      <Map
        ref={mapRef}
        maxBounds={[103.596, 1.1443, 104.1, 1.4835]}
        mapStyle="https://www.onemap.gov.sg/maps/json/raster/mbstyle/Grey.json"
        style={{ width: "100vw", height: "100vh" }}
      >
        {locations.map((location) => (
          <Marker
            key={location.station_code}
            latitude={location.latitude}
            longitude={location.longitude}
          >
            <div
              className="mrt-marker"
              style={{ backgroundColor: getMarkerColor(location.station_code) }}
              onClick={() => flyTo([location.longitude, location.latitude])}
            >
              {/* {location.name} */}
            </div>
          </Marker>
        ))}
      </Map>

      <div className="legend">
        <div><span className="legend-color" style={{ backgroundColor: "rgba(255, 0, 0, 0.7)" }}></span> High Density</div>
        <div><span className="legend-color" style={{ backgroundColor: "rgba(255, 165, 0, 0.7)" }}></span> Medium Density</div>
        <div><span className="legend-color" style={{ backgroundColor: "rgba(0, 255, 0, 0.7)" }}></span> Low Density</div>
        <div><span className="legend-color" style={{ backgroundColor: "rgba(0, 0, 255, 0.5)" }}></span> No Data</div>
        <div style={{ marginTop: "10px" }}></div>
        <div>Current Time: {new Date().toLocaleTimeString("en-SG", { timeZone: "Asia/Singapore" })}</div>
        <div>LTA Livefeed Interval: 10 Minutes</div>
      </div>

      <style jsx>{`
        .mrt-marker {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: black;
          font-size: 12px;
          font-weight: bold;
          text-align: center;
          cursor: pointer;
          animation: blink 2s infinite;
        }

        @keyframes blink {
          0% {
            transform: scale(1);
            opacity: 0.8;
          }
          50% {
            transform: scale(1.5);
            opacity: 0.55;
          }
          100% {
            transform: scale(1);
            opacity: 0.8;
          }
        }

        .legend {
          position: absolute;
          bottom: 20px;
          left: 20px;
          background: white;
          padding: 10px;
          border-radius: 5px;
          box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        .legend div {
          display: flex;
          align-items: center;
          margin-bottom: 5px;
        }

        .legend-color {
          width: 20px;
          height: 20px;
          border-radius: 50%;
          margin-right: 10px;
        }
      `}</style>
    </div>
  );
};

export default IndexPage;
