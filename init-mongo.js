db = db.getSiblingDB('testdb');

db.createCollection("notifications");

db.notifications.insertMany([
  {
    _id: ObjectId("65d7e3a9d3b7f53eb4e4e9b1"),
    userId: 1,
    message: "The auction is finished",
    timestamp: ISODate("2024-03-27T12:00:00Z"),
    read: false
  },
  {
    _id: ObjectId("65d7e3a9d3b7f53eb4e4e9b2"),
    userId: 1,
    message: "The auction have a new bid",
    timestamp: ISODate("2024-03-27T12:30:00Z"),
    read: true
  },
  {
      _id: ObjectId("65d7e3a9d3b7f53eb4e4e9b3"),
      userId: 2,
      message: "The auction have a new bid",
      timestamp: ISODate("2024-03-27T12:30:00Z"),
      read: false
  },
  {
      _id: ObjectId("65d7e3a9d3b7f53eb4e4e9b4"),
      userId: 3,
      message: "The auction have a new bid",
      timestamp: ISODate("2024-03-27T12:30:00Z"),
      read: false
  }
]);

