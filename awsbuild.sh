#!/bin/sh
./mvnw spring-boot:build-image
docker tag davisellwood/website:latest ${AWS_ECR_URL}:latest
aws ecr get-login-password --region ${AWS_ECR_REGION} | docker login --username AWS --password-stdin ${AWS_ECR_URL}
aws ecr create-repository --repository-name ${AWS_ECR_REPO_NAME} || true
docker push ${AWS_ECR_URL}:latest
aws ecs update-service --service ${AWS_ECS_SERVICE} --cluster ${AWS_ECS_CLUSTER}