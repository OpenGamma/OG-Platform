parameters {
  foo String
  bar Double
}

scenario 'scenarioName', {
  marketData {
    id 'SCHEME', foo
    apply {
      scaling bar
    }
  }
}
