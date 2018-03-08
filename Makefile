IMAGE_NAME := koji-build-finder

build:
	docker build --tag=$(IMAGE_NAME) .

shell:
	docker run -v $(CURDIR):/src:Z -w /src -it $(IMAGE_NAME) bash
