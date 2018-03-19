.PHONY: upload

upload:
	zip epilogue.zip ./build/epilogue.jar
	./bin/aws/deploy.sh
