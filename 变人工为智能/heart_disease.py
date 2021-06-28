# -*- coding: utf-8 -*-
"""
Created on Fri Jun 25 14:38:53 2021

@author: 严天成
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import logging
from boto3.session import Session
from botocore.exceptions import ClientError
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import precision_score, recall_score, f1_score, confusion_matrix, classification_report,accuracy_score
data=pd.read_csv("d:/kaggle/dataset/heart.csv")
y=data['target'].values
x=data[['age','sex','cp','trestbps','chol','fbs','restecg','thalach','exang','oldpeak','slope','ca','thal']].values
y=y.reshape(-1,1)
x_train,x_test,y_train,y_test=train_test_split(x,y,test_size=0.2,random_state=42,shuffle=True)

std=StandardScaler()
x_train=std.fit_transform(x_train)
x_test=std.fit_transform(x_test)

model=LogisticRegression()
model.fit(x_train, y_train.astype('int'))

y_pred=model.predict(x_test)
y_pred=y_pred.reshape(-1,1)

accuracy_score_value = accuracy_score(y_test, y_pred)
print(f"准确率:{accuracy_score_value}")

precision_score_value = precision_score(y_test, y_pred)
print(f"精确率:{precision_score_value}")

recall_score_value = recall_score(y_test, y_pred)
print(f"召回率:{recall_score_value}")

f1_score_value = f1_score(y_test, y_pred)
print(f"f1值:{f1_score_value}")

confusion_matrix_value = confusion_matrix(y_test, y_pred)
print(f"混淆矩阵:{confusion_matrix_value}")

report = classification_report(y_test, y_pred)
print(f"分类报告:")
print(report)

access_key = "12BD2990F33681DB1E4C"
secret_key = "W0ExQ0UwQzcxMjVDQjVGNTk4Q0Y3Mjg3MTdEN0U4"
url = "http://scut.depts.bingosoft.net:29997"
session = Session(access_key, secret_key)
s3_client = session.client('s3', endpoint_url=url)
result=pd.concat([pd.Series(y_test.reshape(-1),name="target"),pd.Series(y_pred.reshape(-1),name="predict_target")],axis=1)
x_test=pd.DataFrame(x_test)
result=pd.concat([x_test,result],axis=1)
pd.DataFrame.to_csv(result,path_or_buf="./result/result.csv")
try:
    s3_client.upload_file("./result/result.csv", "ytc", "upload/result.csv", ExtraArgs={'ACL': 'public-read'})
except ClientError as e:
    logging.error(e)