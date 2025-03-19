NAMESPACE := scala-runners

.PHONY: install-controller
install-controller:
  helm install scala-runner-controller \
    --namespace "${NAMESPACE}" \
    --create-namespace \
	--values controller-config.yaml \
    oci://ghcr.io/actions/actions-runner-controller-charts/gha-runner-scale-set-controller

.PHONY: install-scale-set
install-scale-set:
  helm install scala-runner-scale-set \
    --namespace "${NAMESPACE}" \
    --create-namespace \
	--values runner-config.yaml \
    oci://ghcr.io/actions/actions-runner-controller-charts/gha-runner-scale-set

