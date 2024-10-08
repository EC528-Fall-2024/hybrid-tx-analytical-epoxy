// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: worker.proto

package org.dbos.apiary;

public interface ExecuteFunctionRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:frontend.ExecuteFunctionRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>repeated bytes arguments = 2;</code>
   */
  java.util.List<com.google.protobuf.ByteString> getArgumentsList();
  /**
   * <code>repeated bytes arguments = 2;</code>
   */
  int getArgumentsCount();
  /**
   * <code>repeated bytes arguments = 2;</code>
   */
  com.google.protobuf.ByteString getArguments(int index);

  /**
   * <code>repeated int32 argumentTypes = 3;</code>
   */
  java.util.List<java.lang.Integer> getArgumentTypesList();
  /**
   * <code>repeated int32 argumentTypes = 3;</code>
   */
  int getArgumentTypesCount();
  /**
   * <code>repeated int32 argumentTypes = 3;</code>
   */
  int getArgumentTypes(int index);

  /**
   * <code>int64 callerId = 4;</code>
   */
  long getCallerId();

  /**
   * <code>int64 functionId = 5;</code>
   */
  long getFunctionId();

  /**
   * <code>int64 senderTimestampNano = 6;</code>
   */
  long getSenderTimestampNano();

  /**
   * <code>string role = 7;</code>
   */
  java.lang.String getRole();
  /**
   * <code>string role = 7;</code>
   */
  com.google.protobuf.ByteString
      getRoleBytes();

  /**
   * <pre>
   * Unique global IDs for an entire workflow. For retro replay, it means the first execution to be replayed.
   * </pre>
   *
   * <code>int64 executionId = 8;</code>
   */
  long getExecutionId();

  /**
   * <pre>
   * 0: not replay, 1: replay a single request, 2: replay a request and everything after, 3: selective replay.
   * </pre>
   *
   * <code>int32 replayMode = 9;</code>
   */
  int getReplayMode();

  /**
   * <pre>
   * Used for retro replay, the last execution ID of the replay.
   * </pre>
   *
   * <code>int64 endExecId = 10;</code>
   */
  long getEndExecId();
}
