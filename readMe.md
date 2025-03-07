# MRT Crowd Density Tracker

## Overview
The MRT Crowd Density Tracker is a real-time application designed to monitor and display the crowd density in MRT stations (Interchange). This tool helps commuters plan their journeys more efficiently by providing up-to-date information on station congestion levels.

![Crowd Density Tracker in Action](gif.mov)

## Features
- **Real-time Data**: Fetches live updates from LTA on crowd density at various MRT stations and uses Kafka for real-time processing. For more info on datasource [see](https://datamall.lta.gov.sg/content/datamall/en.html)
- **User-friendly Interface**: Easy-to-navigate and interactive Map interface
- **Dockerized Setup**: Run the entire application in less than a minute using Docker Compose.


---

## Installation & Setup (Using Docker Compose)

### **Prerequisites**
- Ensure you have **Docker** and **Docker Compose** installed on your system.
- Fetch your LTA DataMall API [here](https://datamall.lta.gov.sg/content/datamall/en/request-for-api.html)

### **1. Clone the Repository**
```bash
git clone https://github.com/yourusername/MRT-Crowd-Tracker.git
cd MRT-Crowd-Tracker
```

### **2. Start the Application with Docker Compose**
```bash
docker-compose up --build
```
This command will:
- Start **Zookeeper** and **Kafka**
- Deploy **MongoDB** and **Mongo Express (UI for MongoDB)**
- Launch the **Spring Boot Backend** (Fetches livefeed from LTA, Produce and Consumes them as Kafka messages, store in mongodb and serves API for frontend)
- Run the **Next.js Frontend** (Displays crowd data on SG map)

### **3. Access the Application**
Once the containers are running, access map via:
- **Frontend**: [http://localhost:3000](http://localhost:3000)
To check MongoDB use:
- **MongoDB Admin Panel**: [http://localhost:8081](http://localhost:8081) (Login: `user1` / `Password123`) 

### **4. Stopping the Application**
To stop and remove the containers:
```bash
docker-compose down
```

---

## Contributing
We welcome contributions! Please read our [Contributing Guidelines](CONTRIBUTING.md) for more details.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.

## Contact
For any inquiries or feedback, please contact me at [here](mailto:raphaelliew1@gmail.com).

