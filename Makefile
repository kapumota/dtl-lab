# Makefile principal de DLT-Lab para validacion, pruebas, demostraciones y verificacion.
.PHONY: validate test demo demo-mempool demo-defi demo-adversarial security formal formal-research trace-export conformance-replay conformance-negative conformance-research conformance-structure clean

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

formal-research:
	bash scripts/formal/run_formal_research.sh

trace-export:
	bash scripts/export_trace_catalog.sh

conformance-replay:
	bash scripts/conformance/run_trace_replay.sh

conformance-negative:
	bash scripts/conformance/run_negative_trace_corpus.sh

conformance-research:
	bash scripts/conformance/run_conformance_research.sh

conformance-structure:
	bash scripts/conformance/check_conformance_structure.sh

clean:
	rm -rf build target out
	find . -type f -name "*.class" -delete
	find . -type f -name "*.jar" -delete
	find . -type f -name "*.war" -delete
	find . -type f -name "*.ear" -delete
