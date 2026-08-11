# Makefile principal de DLT-Lab para validacion, pruebas, demostraciones y verificacion.
.PHONY: validate test demo demo-mempool demo-defi demo-adversarial security formal formal-research trace-export conformance-replay conformance-negative conformance-research conformance-structure experiment-protocol experiment-infrastructure experiment-scientific-structure experiment-scientific-smoke experiment-matrix experiment-analysis-structure experiment-analysis experiment-reproduction-structure experiment-reproduction-prepare experiment-reproduction-bundle experiment-reproduction clean

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

experiment-protocol:
	bash scripts/experiments/check_experimental_structure.sh

experiment-infrastructure:
	bash scripts/experiments/check_experiment_infrastructure.sh

experiment-scientific-structure:
	bash scripts/experiments/check_scientific_matrix_structure.sh

experiment-scientific-smoke:
	bash scripts/experiments/run_scientific_matrix.sh smoke

experiment-matrix:
	bash scripts/experiments/run_scientific_matrix.sh definitive

experiment-analysis-structure:
	bash scripts/experiments/check_experiment_analysis_structure.sh

experiment-analysis:
	bash scripts/experiments/run_experiment_analysis.sh

experiment-reproduction-structure:
	bash scripts/experiments/check_independent_reproduction_structure.sh

experiment-reproduction-prepare:
	bash scripts/formal/install_tla_tools.sh
	bash scripts/formal/install_alloy.sh

experiment-reproduction-bundle:
	@test -n "$(RAW_ARCHIVE)" || (echo "RAW_ARCHIVE es obligatorio." >&2; exit 2)
	@test -n "$(RAW_CHECKSUM)" || (echo "RAW_CHECKSUM es obligatorio." >&2; exit 2)
	@test -n "$(REPRODUCTION_BUNDLE)" || (echo "REPRODUCTION_BUNDLE es obligatorio." >&2; exit 2)
	python3 -B scripts/experiments/reproduction_bundle.py build \
		--repository-root . \
		--raw-archive "$(RAW_ARCHIVE)" \
		--raw-checksum "$(RAW_CHECKSUM)" \
		--derived-dir results/experiments/derived/paper1-q3-v1 \
		--tables-dir results/experiments/tables/paper1-q3-v1 \
		--figures-dir results/experiments/figures/paper1-q3-v1 \
		--output-archive "$(REPRODUCTION_BUNDLE)"

experiment-reproduction:
	@test -n "$(REPRODUCTION_BUNDLE)" || (echo "REPRODUCTION_BUNDLE es obligatorio." >&2; exit 2)
	bash scripts/experiments/run_independent_reproduction.sh \
		"$(REPRODUCTION_BUNDLE)" \
		"$(or $(REPRODUCTION_OUTPUT),results/experiments/reproduction/paper1-q3-v1)"

clean:
	rm -rf build target out
	find . -type f -name "*.class" -delete
	find . -type f -name "*.jar" -delete
	find . -type f -name "*.war" -delete
	find . -type f -name "*.ear" -delete
