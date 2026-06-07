.PHONY: validate test demo demo-mempool demo-defi demo-adversarial security formal clean

validate:
	bash scripts/validate.sh

test:
	bash scripts/run_tests.sh

demo:
	bash scripts/run_demo.sh

demo-mempool:
	bash scripts/run_mempool_demo.sh

demo-defi:
	bash scripts/run_defi_mev_demo.sh

demo-adversarial:
	bash scripts/run_adversarial_demo.sh

security:
	bash scripts/run_security_checks.sh

formal:
	bash scripts/run_formal_checks.sh

clean:
	rm -rf build target out
	find . -type f -name "*.class" -delete
	find . -type f -name "*.jar" -delete
	find . -type f -name "*.war" -delete
	find . -type f -name "*.ear" -delete
