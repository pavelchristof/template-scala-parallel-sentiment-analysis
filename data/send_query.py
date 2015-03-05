"""
Send sample query to prediction engine
"""

import predictionio
import readline

engine_client = predictionio.EngineClient(
    url = "http://localhost:8000",
    timeout = 60
)
while True:
    text = raw_input('Enter a sentence: ')
    print engine_client.send_query({"text": text})
