import numpy as np
import json
import torch
from transformers import RobertaForSequenceClassification, AutoTokenizer

# Load model
def load_model():
	# Khởi tạo model
    model = RobertaForSequenceClassification.from_pretrained("wonrax/phobert-base-vietnamese-sentiment")
    print("Load model complete!")
    return model
		
def load_tokenizer():
    tokenizer = AutoTokenizer.from_pretrained("wonrax/phobert-base-vietnamese-sentiment", use_fast=False)
    print("Load tokenizer complete!")
    return tokenizer

def predict_sentiment(model, tokenizer, sentence: str):
    if tokenizer is None:
        raise ValueError("Tokenizer is not initialized!")
    input_ids = torch.tensor([tokenizer.encode(sentence, max_length=256, truncation=True)])
    with torch.no_grad():
        out = model(input_ids)
        probs = out.logits.softmax(dim=-1).tolist()[0]
    
    # Map class index to labels
    labels = ["NEG", "POS", "NEU"]
    results = {label: round(prob, 4) for label, prob in zip(labels, probs)}

    # Get the most likely label
    predicted_label = labels[probs.index(max(probs))]

    return {
        "label": predicted_label,
        "scores": results
    }
    

# Encoding numpy to json
class NumpyEncoder(json.JSONEncoder):
    '''
    Encoding numpy into json
    '''
    def default(self, obj):
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        if isinstance(obj, np.int32):
            return int(obj)
        if isinstance(obj, np.int64):
            return int(obj)
        if isinstance(obj, np.float32):
            return float(obj)
        if isinstance(obj, np.float64):
            return float(obj)
        return json.JSONEncoder.default(self, obj)