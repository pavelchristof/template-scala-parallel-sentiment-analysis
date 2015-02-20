import predictionio
import fileinput
import csv

client = predictionio.EventClient(
    access_key = "Mgd6ZwXAeOVW7cTcOX9BnA9UOD8M2397ShGGjmLS5j5cC6olr0yLpBw7WSivM3ej",
    url = "http://localhost:7070",
    threads = 8,
    qsize = 500
)

reader = csv.DictReader(fileinput.input(), delimiter=",", quotechar='"')
for row in reader:
    client.acreate_event(
        event = "tweet",
        entity_type = "source",
        entity_id = row["SentimentSource"],
        properties = {
            "sentiment": row["Sentiment"],
            "text": row["SentimentText"]
        }
    )

client.close()

