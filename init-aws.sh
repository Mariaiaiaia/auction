#!/bin/bash

echo "Ожидаем, пока LocalStack будет готов..."
until awslocal s3 ls; do
  echo "LocalStack еще не готов. Ждем..."
  sleep 2
done

echo "Создаем S3 бакет..."
awslocal s3 mb s3://auction-item

awslocal s3 ls