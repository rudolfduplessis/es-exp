import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport

/**
  * Created by rudolf on 2017/09/04.
  */
trait ThriftCompactCodec {
  final def encode[T <: ThriftStruct](structCodec: ThriftStructCodec[T], struct: T): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    structCodec.encode(struct, new TCompactProtocol(new TIOStreamTransport(out)))
    out.toByteArray
  }
  final def decode[T <: ThriftStruct](structCodec: ThriftStructCodec[T], data: Array[Byte]): T = {
    val stream = new ByteArrayInputStream(data)
    structCodec.decode(new TCompactProtocol(new TIOStreamTransport(stream)))
  }
}
