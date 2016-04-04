/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.ucm.fdi.sscheck.spark.streaming

import org.apache.spark.streaming._
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._

import scala.language.implicitConversions
import scala.reflect.ClassTag
import org.apache.spark.streaming.dstream.SscheckFriendlyInputDStream
// import org.apache.spark.streaming.dstream.FriendlyInputDStream

/**
 * Copied from spark-testing-base https://github.com/holdenk/spark-testing-base/blob/f66fd14b6a87efc09cd26c302740f3b0fb2585ae/src/main/1.3/scala/com/holdenkarau/spark/testing/TestInputStream.scala
 * Copied here to avoid conflicting version problems with spark-testing-base, see https://github.com/juanrh/sscheck/issues/36
 * This is an almost verbatim copy, just extending SscheckFriendlyInputDStream instead of FriendlyInputDStream,
 * to avoid naming conflicts
 * 
 * This is a input stream just for the testsuites. This is equivalent to a checkpointable,
 * replayable, reliable message queue like Kafka. It requires a sequence as input, and
 * returns the i_th element at the i_th batch unde manual clock.
 * Based on TestInputStream class from TestSuiteBase in the Apache Spark project.
 */
class TestInputStream[T: ClassTag](@transient var sc: SparkContext,
  ssc_ : StreamingContext, input: Seq[Seq[T]], numPartitions: Int)
  extends SscheckFriendlyInputDStream[T](ssc_) {

  def start() {}

  def stop() {}
  
  def compute(validTime: Time): Option[RDD[T]] = {
    logInfo("Computing RDD for time " + validTime)
    val index = ((validTime - ourZeroTime) / slideDuration - 1).toInt
    val selectedInput = if (index < input.size) input(index) else Seq[T]()

    // lets us test cases where RDDs are not created
    if (selectedInput == null) {
      return None
    }

    val rdd = sc.makeRDD(selectedInput, numPartitions)
    logInfo("Created RDD " + rdd.id + " with " + selectedInput)
    Some(rdd)
  }
}