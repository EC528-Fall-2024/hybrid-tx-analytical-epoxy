// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: worker.proto

package org.dbos.apiary;

public interface ExecuteFunctionReplyOrBuilder extends
    // @@protoc_insertion_point(interface_extends:frontend.ExecuteFunctionReply)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string replyString = 1;</code>
   */
  java.lang.String getReplyString();
  /**
   * <code>string replyString = 1;</code>
   */
  com.google.protobuf.ByteString
      getReplyStringBytes();

  /**
   * <code>int32 replyInt = 2;</code>
   */
  int getReplyInt();

  /**
   * <code>bytes replyArray = 7;</code>
   */
  com.google.protobuf.ByteString getReplyArray();

  /**
   * <code>int64 callerId = 3;</code>
   */
  long getCallerId();

  /**
   * <code>int64 functionId = 4;</code>
   */
  long getFunctionId();

  /**
   * <code>int64 senderTimestampNano = 5;</code>
   */
  long getSenderTimestampNano();

  /**
   * <code>int64 replyType = 6;</code>
   */
  long getReplyType();

  /**
   * <pre>
   * If the invocation failed, we can add an error message.
   * </pre>
   *
   * <code>string errorMsg = 8;</code>
   */
  java.lang.String getErrorMsg();
  /**
   * <pre>
   * If the invocation failed, we can add an error message.
   * </pre>
   *
   * <code>string errorMsg = 8;</code>
   */
  com.google.protobuf.ByteString
      getErrorMsgBytes();
}
