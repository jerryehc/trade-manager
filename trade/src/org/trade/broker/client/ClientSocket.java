/* ===========================================================
 * TradeManager : a application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.broker.client;

import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingWorker;

import org.trade.broker.BrokerModelException;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Tradestrategy;

public class ClientSocket {

	private static final ConcurrentHashMap<Integer, SwingWorker<Void, Void>> m_backTestBroker = new ConcurrentHashMap<Integer, SwingWorker<Void, Void>>();
	private ClientWrapper m_client = null;

	public ClientSocket(ClientWrapper client) {
		m_client = client;
	}

	/**
	 * Method reqHistoricalData.
	 * 
	 * @param reqId
	 *            int
	 * @param ibContract
	 *            com.ib.client.Contract
	 * @param endDateTime
	 *            String
	 * @param durationStr
	 *            String
	 * @param barSizeSetting
	 *            String
	 * @param whatToShow
	 *            String
	 * @param useRTH
	 *            int
	 * @param formatDateInteger
	 *            int
	 * @throws BrokerModelException
	 */
	public void reqHistoricalData(int reqId, Contract contract,
			String endDateTime, String durationStr, String barSizeSetting,
			String whatToShow, int useRTH, int formatDateInteger)
			throws BrokerModelException {

		try {

			if (null != endDateTime) {

				YahooBroker yahooBroker = new YahooBroker(contract,
						endDateTime, durationStr, barSizeSetting, m_client);
				m_backTestBroker.put(contract.getIdContract(), yahooBroker);
				yahooBroker.execute();

			} else {
				for (Tradestrategy tradestrategy : contract
						.getTradestrategies()) {
					if (tradestrategy.getTrade()) {
						BackTestBroker backTestBroker = new BackTestBroker(
								tradestrategy.getDatasetContainer(),
								tradestrategy.getIdTradeStrategy(), m_client);
						m_backTestBroker.put(
								tradestrategy.getIdTradeStrategy(),
								backTestBroker);
						backTestBroker.execute();
					}
				}
				m_client.historicalData(reqId,
						"finished- at yyyyMMdd HH:mm:ss", 0, 0, 0, 0, 0, 0, 0,
						false);
			}
		} catch (Exception ex) {
			throw new BrokerModelException(0, 6000,
					"Error initializing BackTestBroker Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method removeBackTestBroker.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 */

	public void removeBackTestBroker(Integer idTradestrategy) {
		synchronized (m_backTestBroker) {
			SwingWorker<Void, Void> worker = m_backTestBroker
					.get(idTradestrategy);
			if (null != worker) {
				if (worker.isDone() || worker.isCancelled()) {
					m_backTestBroker.remove(idTradestrategy);
				}
			}
		}
	}

	/**
	 * Method getBackTestBroker.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * 
	 * @return BackTestBroker
	 */

	public SwingWorker<Void, Void> getBackTestBroker(Integer idTradestrategy) {
		return m_backTestBroker.get(idTradestrategy);
	}

	/**
	 * Method getBackTestBroker.
	 * 
	 * @param reqId
	 *            int
	 * @param contract
	 *            Contract
	 * @param barSize
	 *            int
	 * @param whatToShow
	 *            String
	 * @param useRTH
	 *            boolean
	 */
	public void reqRealTimeBars(int reqId, Contract contract, int barSize,
			String whatToShow, boolean useRTH) {
	}
}