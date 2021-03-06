package akka.stream.checkpoint

import com.codahale.metrics.MetricRegistry
import org.scalatest.{MustMatchers, WordSpec}

class DropwizardCheckpointRepositorySpec extends WordSpec with MustMatchers {

  "DropwizardCheckpointRepository" should {

    val registry = new MetricRegistry()
    val repository = DropwizardCheckpointRepository("test")(registry)

    "store readings in aptly named metrics" when {

      "elements are pulled into the checkpoint" in {
        val latency = 42L
        repository.markPull(latency)

        registry.histogram("test_pull_latency").getCount must ===(1)
        registry.histogram("test_pull_latency").getSnapshot.getValues must ===(Array(latency))

        registry.counter("test_backpressured").getCount must ===(0)
      }

      "elements are pushed through the checkpoint" in {
        val latency = 64L
        val backpressureRatio = 54L
        repository.markPush(latency, backpressureRatio)

        registry.histogram("test_push_latency").getCount must ===(1)
        registry.histogram("test_push_latency").getSnapshot.getValues must ===(Array(latency))

        registry.histogram("test_backpressure_ratio").getCount must ===(1)
        registry.histogram("test_backpressure_ratio").getSnapshot.getValues must ===(Array(backpressureRatio))

        registry.meter("test_throughput").getCount must ===(1)

        registry.counter("test_backpressured").getCount must ===(1)
      }
    }
  }
}
