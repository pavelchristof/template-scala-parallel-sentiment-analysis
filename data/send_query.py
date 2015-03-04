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
    word = raw_input('Enter a word: ')
    print engine_client.send_query({"word": word, "num": 10})
