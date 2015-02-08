import predictionio
import csv

client = predictionio.EventClient(
    access_key = "hPwDhIttGQgycNhJvL1Co9LAO2cKR8g70VeuuLmD5pqhwbG7v54ztSjZWsg8Jdtp",
    url = "http://localhost:7070",
    threads = 5,
    qsize = 500
)

with open("./data/dataset.csv", "rb") as csvfile:
    reader = csv.DictReader(csvfile, delimiter=",", quotechar='"')
    for row in reader:
        client.create_event(
            event = "tweet",
            entity_type = "source",
            entity_id = row["SentimentSource"],
            properties = {
                "sentiment": row["Sentiment"],
                "text": row["SentimentText"]
            }
        )
