package com.yammer.jmx

import scala.collection.mutable
import java.util.concurrent.TimeUnit
import com.yammer.metrics.{LoadMeter, Meter, Timer, Counter}

/**
 * A builder class for JmxBeans.
 */
class JmxBeanBuilder(description: String, obj: AnyRef) {
  private val attributes = mutable.Map[String, JmxReadOnlyAttribute]()

  /**
   * Add an arbitrary read-only JMX attribute.
   */
  def addAttribute(name: String, description: String = null)(callback: => Any) {
    attributes += (name -> JmxReadOnlyAttribute(name, description, callback _))
  }

  /**
   * Add a Counter as a JMX attribute.
   */
  def addCounter(name: String, counter: Counter) {
    addAttribute("%s-count".format(name)) { counter.count }
  }

  /**
   * Add a Meter as a JMX attribute. (Adds count and rate values.)
   */
  def addMeter(name: String, meter: Meter, unit: TimeUnit) {
    addAttribute("%s-count".format(name)) { meter.count }
    addAttribute("%s-rate".format(name)) { meter.rate.convert(unit) }
  }

  /**
   * Adds a LoadMeter as a JMX attribute. (Adds count, mean rate, 1-minute rate,
   * 5-minute rate, and 15-minute rate values.)
   */
  def addLoadMeter(name: String, meter: LoadMeter, unit: TimeUnit) {
    addAttribute("%s-count".format(name)) { meter.count }
    addAttribute("%s-mean-rate".format(name)) { meter.rate.convert(unit) }
    addAttribute("%s-01min-rate".format(name)) { meter.oneMinuteRate.convert(unit) }
    addAttribute("%s-05min-rate".format(name)) { meter.fiveMinuteRate.convert(unit) }
    addAttribute("%s-15min-rate".format(name)) { meter.fifteenMinuteRate.convert(unit) }
  }

  /**
   * Add a Timer as a JMX attribute. (Adds count, max, min, mean, stddev, and
   * p999 values.)
   */
  def addTimer(name: String, timer: Timer, unit: TimeUnit) {
    addAttribute("%s-count".format(name)) { timer.count }
    addAttribute("%s-max".format(name)) { timer.max.convert(unit) }
    addAttribute("%s-min".format(name)) { timer.min.convert(unit) }
    addAttribute("%s-mean".format(name)) { timer.mean.convert(unit) }
    addAttribute("%s-stddev".format(name)) { timer.standardDeviation.convert(unit) }
    addAttribute("%s-99.9%%".format(name)) { timer.p999.convert(unit) }
  }

  /**
   * Build a JmxBean with the added attributes.
   */
  def build = JmxBean(obj.getClass, description, attributes.toMap)
}
