[README.md](https://github.com/user-attachments/files/23870499/README.md)
# FoodDeliveryEcosystem

A multi-enterprise Java Food Delivery System with:
- Java Swing/JavaFX UI
- MySQL Database (Docker Hosted)
- JDBC Connectivity
- Multi-role access (Customer, Restaurant Admin, Delivery Admin, Riders)
- Complete CRUD Features

This repository contains:
- Source code
- Database schema
- Presentation slides
- Recorded demo link

More updates coming as development progresses.

## Running locally with Docker MySQL

1. Start a containerized MySQL server (Docker must be installed):

```bash
docker-compose up -d --build
```

This project maps the container's MySQL port to host `3307` to avoid conflicts with any local MySQL server.

2. Confirm the DB and tables were created (schema is mounted into the container):

```bash
mysql -h 127.0.0.1 -P 3307 -u user -ppassword -e "SHOW TABLES;" -D food_delivery_db
```

3. Update `src/main/resources/db.properties` if you want to use different credentials or ports. The app uses this file at runtime to connect to the DB.

4. Build and run the app:

```bash
mvn -DskipTests=false package
mvn -Dexec.classpathScope=test -Dexec.mainClass=database.TestDBConnection org.codehaus.mojo:exec-maven-plugin:3.1.0:java
```

5. Connect using MySQL Workbench / client using these settings:
- Host: 127.0.0.1
- Port: 3307
- Username: user
- Password: password
- Database: food_delivery_db

## Seed data and demo script
We include a `db/schema.sql` file with table creation and seed data. If you've already started the container and want to recreate the seed data, either:

1) Remove the Docker volume and restart to re-run init scripts (destructive):

```bash
docker-compose down -v
docker-compose up -d --build
```

2) Or run the schema script directly (this will run on the current DB and avoid destroying data):

```bash
mysql -h 127.0.0.1 -P 3307 -u user -ppassword -D food_delivery_db < db/schema.sql
```

## Demo script
We've added a convenience script `run-demo.sh` that will bring up Docker and run the application for demo purposes.

```bash
./run-demo.sh
```

## Diagrams, screenshots, and presentation
Add your class and use case diagrams (image files) into the `images/` directory. Add your recorded presentation link in the README or a dedicated `presentation.txt` file.

## New Features

- Enterprise Admin Role: There is now an `EnterpriseAdmin` role with a dedicated `EnterpriseAdminWorkArea` UI. This lets enterprise administrators manage organizations, users, and inter-enterprise work requests for their enterprise only.
- Work Requests: The system now supports formal inter-enterprise work requests (`work_requests` table). Request types include `OrderRequest`, `SupplyRequest`, and `DeliveryAssignment`. Enterprise Admins can create requests and attach related orders if relevant.

## Login for demo

The seed DB includes sample credentials to demo functionality:
- System Admin: `sysadmin` / `sysadmin`
- Enterprise Admin for Boston: `entadmin1` / `entadmin1`
- Manager: `manager1` / `manager1`
- Delivery Man: `delivery1` / `delivery1` and `delivery2` / `delivery2`

## Testing Work Requests

To test the Work Request flow:
1. Login as `entadmin1`.
2. Open the `Work Requests` tab and create a new request. Attach a related order if prompted.
3. Login as the receiver enterprise admin to view and update the request status.



