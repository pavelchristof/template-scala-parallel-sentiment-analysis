import predictionio
import fileinput
import csv

client = predictionio.EventClient(
    access_key = "CYwhPig17a1z7eA88S0WsiwAI50zDB3ASp3qdblpRw19Wghy0TYehbKgL71S2dXX",
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
