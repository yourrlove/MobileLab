import json
from flask import Flask, jsonify, request
import hyper as hp
import logging
import time
from utils import load_model, load_tokenizer, predict_sentiment

# Khởi tạo model.
global model, tokenizer
model = tokenizer = None


logging.basicConfig(level=logging.INFO)


# Khởi tạo flask app
app = Flask(__name__)
# route và method
@app.route("/", methods=["GET"])
# Hàm xử lý dữ liệu
def _hello_world():
	return "Hello world"


@app.route("/predict", methods=["POST"])
def _predict():
    if not request.is_json:
        return jsonify({"success": False, "error": "Invalid or missing JSON"}), 400

    data = request.get_json()
    sentence = data.get("sentence")

    if not sentence:
        return jsonify({"success": False, "error": "Missing 'sentence' field"}), 400

    result = predict_sentiment(model=model, tokenizer=tokenizer, sentence=sentence)
    return jsonify({"success": True, "result": result})

if __name__ == "__main__":
    # Log start time
    start_time = time.time()
    logging.info(f"App started at {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(start_time))}")

    # Load model and tokenizer
    model = load_model()
    tokenizer = load_tokenizer()
    
    # Log end time
    end_time = time.time()
    logging.info(f"App ended at {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(end_time))}")
    logging.info(f"Total runtime: {end_time - start_time:.2f} seconds")
    
    logging.info("App run!")
    # Run the Flask app
    app.run(debug=False, host=hp.IP, threaded=False)

