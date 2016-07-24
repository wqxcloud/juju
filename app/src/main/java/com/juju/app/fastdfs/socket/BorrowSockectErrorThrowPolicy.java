package com.juju.app.fastdfs.socket;

import com.juju.app.fastdfs.exception.FdfsConnectException;
import com.juju.app.fastdfs.exception.FdfsUnavailableException;

import java.net.InetSocketAddress;


public class BorrowSockectErrorThrowPolicy implements IBorrowSockectErrorPolicy {

	@Override
	public FdfsSocket handleWhenErrorOccur(FdfsSocketPool pool,
			InetSocketAddress address, Exception ex) {
		
		Throwable e = ex; 
		int i = 0;
		while (e != null && i < 5) {
			if (e instanceof FdfsConnectException) {
				throw (FdfsConnectException)e;
			}
			e = e.getCause();
			i++;
		}
		
		throw new FdfsUnavailableException("连接池中无可用资源", ex);
	}

}
