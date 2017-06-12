package cl.helpcom.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.text.StyledEditorKit.BoldAction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cl.helpcom.dao.ConexionSql1;

public class LeerXML {

	private String rutEmisor;
	private String rutEnvia;
	private Long trackID;
	private String tmstRecepcion;
	private String estado1;

	private Integer[][] subTotal;
	private String[][] revisionDTE;
	private String[][] detalleRevisionDTE;
	private Integer ere_id;

	private Boolean isReparo = Boolean.FALSE;
	private Boolean isRechazo = Boolean.FALSE;

	/**
	 * @param fileResSII
	 *            Archivo Resultado
	 * @return 0=OTRO; 1=ACEPTA; 2=REPARO; 3=RECHAZO
	 */
	public Integer getResultSII(String fileResSII) {

		String out = "";

		try {
			File file = new File(fileResSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			// System.out.println("Root element " +
			// doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("SUBTOTAL");
			// System.out.println("Information of all employees");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;

					// ACEPTA
					try {
						// System.out.println(fstElmnt.hasAttribute("RECHAZO"));
						NodeList RENmElmntLst = fstElmnt.getElementsByTagName("ACEPTA");
						Element RENmElmnt = (Element) RENmElmntLst.item(0);
						NodeList RENm = RENmElmnt.getChildNodes();
						out = ((Node) RENm.item(0)).getNodeValue();

						return 1;

					} catch (Exception e) {

					}
					// REPARO
					try {
						// System.out.println(fstElmnt.hasAttribute("RECHAZO"));
						NodeList RENmElmntLst = fstElmnt.getElementsByTagName("REPARO");
						Element RENmElmnt = (Element) RENmElmntLst.item(0);
						NodeList RENm = RENmElmnt.getChildNodes();
						out = ((Node) RENm.item(0)).getNodeValue();
						this.setIsReparo(Boolean.TRUE);
						return 2;

					} catch (Exception e) {

					}
					// RECHAZO
					try {
						// System.out.println(fstElmnt.hasAttribute("RECHAZO"));
						NodeList RENmElmntLst = fstElmnt.getElementsByTagName("RECHAZO");
						Element RENmElmnt = (Element) RENmElmntLst.item(0);
						NodeList RENm = RENmElmnt.getChildNodes();
						out = ((Node) RENm.item(0)).getNodeValue();
						this.setIsRechazo(Boolean.TRUE);
						return 3;

					} catch (Exception e) {

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	public String getTipoDTE(String dte) {

		String tipoDTE = "";
		String Salida = "";

		try {
			File file = new File(dte);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			// System.out.println("Root element " +
			// doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("IdDoc");
			// System.out.println("Information of all employees");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;

					NodeList tipoDTEmElmntLst = fstElmnt.getElementsByTagName("TipoDTE");
					// NodeList RENmElmntLst = fstElmnt.getChildNodes();
					Element tipoDTEmElmnt = (Element) tipoDTEmElmntLst.item(0);
					NodeList RENm = tipoDTEmElmnt.getChildNodes();
					// System.out.println("<DD>"+ ((Node)
					// RENm.item(0)).getNodeValue() +"</DD>");
					Salida = ((Node) RENm.item(0)).getNodeValue();

				}
			}
		} catch (Exception e) {

		}

		return Salida;
	}

	/**
	 * Captura los valores del Tag<<IDENTIFICACION>>
	 * 
	 * @param docSII
	 *            Documento Respuesta SII
	 */
	public void capturaValoresIdentificacion(String docSII) {

		try {
			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("IDENTIFICACION");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTEMISOR");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						this.setRutEmisor(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					System.out.println("\tProblema al obtener <RUTEMISOR> de tag <IDENTIFICACION>");
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTENVIA");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						this.setRutEnvia(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					System.out.println("\tProblema al obtener <RUTENVIA> de tag <IDENTIFICACION>");
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TRACKID");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						this.setTrackID(Long.valueOf(((Node) RENm.item(0)).getNodeValue()));
					}
				} catch (Exception e) {
					System.out.println("\tProblema al obtener <TRACKID> de tag <IDENTIFICACION>");
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TMSTRECEPCION");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						this.setTmstRecepcion(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					System.out.println("\tProblema al obtener <TMSTRECEPCION> de tag <IDENTIFICACION>");
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("ESTADO");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						this.setEstado1(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					System.out.println("\tProblema al obtener <ESTADO> de tag <IDENTIFICACION>");
				}

			}
		} catch (Exception e) {

		}
	}

	public void capturaValoresSubTotal2(String docSII) {

		Integer[][] subTot = new Integer[10][10];

		try {

			File fXmlFile = new File(docSII);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("SUBTOTAL");
			// System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					subTot[temp][0] = Integer
							.valueOf(eElement.getElementsByTagName("TIPODOC").item(0).getTextContent());
					subTot[temp][1] = Integer
							.valueOf(eElement.getElementsByTagName("INFORMADO").item(0).getTextContent());
					try {
						subTot[temp][2] = Integer
								.valueOf(eElement.getElementsByTagName("ACEPTA").item(0).getTextContent());
					} catch (Exception e) {
						// No tiene ACEPTA
					}
					try {
						subTot[temp][3] = Integer
								.valueOf(eElement.getElementsByTagName("RECHAZO").item(0).getTextContent());
					} catch (Exception e) {

					}
					try {

						subTot[temp][4] = Integer
								.valueOf(eElement.getElementsByTagName("REPARO").item(0).getTextContent());
					} catch (Exception e) {

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.setSubTotal(subTot);
	}

	/**
	 * 
	 * Por defecto en la funcion anterior todos los documentos con el Track Id
	 * tienen el mismo estado ACEPTADO En esta funcion cambian a REPARO O
	 * RECHAZADO aquellos que dentro del paquete están con ese estado
	 * 
	 * @param docSII
	 * @param emp_id
	 * @param trackID
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public void capturaValoresRevisionDTE2(String docSII, Integer emp_id, Long trackID)
			throws ClassNotFoundException, SQLException, IOException {

		String[][] revDTE = new String[200][10];
		String[][] detalleRevDTE = new String[155][10];

		// ArrayList<ArrayList<String>> revDTE = new
		// ArrayList<ArrayList<String>>();
		// ArrayList<ArrayList<String>> detalleRevDTE = new
		// ArrayList<ArrayList<String>>();
		ConexionSql1 con = new ConexionSql1();
		int conY = 0;
		int conX = 0;
		try {
			File fXmlFile = new File(docSII);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("REVISIONDTE");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					revDTE[temp][0] = eElement.getElementsByTagName("FOLIO").item(0).getTextContent();
					revDTE[temp][1] = eElement.getElementsByTagName("TIPODTE").item(0).getTextContent();
					revDTE[temp][2] = eElement.getElementsByTagName("ESTADO").item(0).getTextContent();

					// verificarAcuse () devuelve un String con el valor del
					// doc_respuesta_sii(NINGUNO, ACEPTADO, RECHAZADO, ACEPTADO
					// CON REPARO)

					String verificar = con.VerificarAcuse(Integer.valueOf(revDTE[temp][0]), emp_id,
							Integer.valueOf(revDTE[temp][1]));
					System.out.println("\tFolio: " + revDTE[temp][0]);
					System.out.println("\tTipo DTE: " + revDTE[temp][1]);
					System.out.println("\tEmp ID: " + emp_id);

					// Si el metodo verificar() devuelve valor aceptado o
					// aceptado con reparos, no es necesario modificar el
					// registro porque
					// fue aceptado con anterioridad.
					if ((verificar.equalsIgnoreCase("ACEPTADO") || verificar.equalsIgnoreCase("ACEPTADO CON REPARO"))) {

						System.out.println("\tRegistro ya fue aceptado con anterioridad por SII");

					} else {
						// Si el metodo verificar devuelve RECHAZADO O NINGUNO,
						// se procede a cambiar de estado.
						if (revDTE[temp][2].startsWith("RPR")) {
							// CAMBIAR ESTADO A REPARO FOLIO,TIPO,EMP_ID
							con.updateEstado(Integer.valueOf(revDTE[temp][0]), emp_id, Integer.valueOf(revDTE[temp][1]),
									"ACEPTADO CON REPARO");
							con.addEnviosRechazo(trackID, emp_id, Integer.valueOf(revDTE[temp][1]),
									Integer.valueOf(revDTE[temp][0]), revDTE[temp][2]);
							this.setEre_id(con.getEre_Id(trackID, emp_id, Integer.valueOf(revDTE[temp][0]),
									Integer.valueOf(revDTE[temp][1])));

						} else if (revDTE[temp][2].startsWith("RCH")) {
							// CAMBIAR ESTADO A RECHAZO FOLIO,TIPO,EMP_ID
							con.updateEstado(Integer.valueOf(revDTE[temp][0]), emp_id, Integer.valueOf(revDTE[temp][1]),
									"RECHAZADO");
							// Agregar a registros "rec_emi_envios_rechazados"
							con.addEnviosRechazo(trackID, emp_id, Integer.valueOf(revDTE[temp][1]),
									Integer.valueOf(revDTE[temp][0]), revDTE[temp][2]);
							this.setEre_id(con.getEre_Id(trackID, emp_id, Integer.valueOf(revDTE[temp][0]),
									Integer.valueOf(revDTE[temp][1])));
						}
					}

					try {
						for (int i = 0; i < 10; i++) {
							detalleRevDTE[temp][i] = eElement.getElementsByTagName("DETALLE").item(i).getTextContent();
							con.addEnviosRechazoDetalle(this.getEre_id(), detalleRevDTE[temp][i]);
						}
					} catch (Exception e) {
						// System.out.println("Problema al obtener DETALLE de
						// EMP: "+emp_id+"Folio: "+revDTE[temp][0]+ "TIPO
						// :"+revDTE[temp][1]);
					}

				}

			}
			// Aceptar todos los que no fueron reparo ni rechazo y tengan estado
			// NINGUNA
			con.updateEstadoAcepta("ACEPTADO", emp_id, trackID);
			this.setRevisionDTE(revDTE);
			this.setDetalleRevisionDTE(detalleRevDTE);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Llena array con datos de Acuse si el documento corresponde con dicho
	 * formato de lo contrario entrega un Array con size() = 0
	 * 
	 * POSICION ARRAY: 0-> Rut Responde 1-> RUT Recibe (Emp Actual)
	 * 
	 * @param docSII
	 *            ruta de carpeta de documentos a analizar
	 * @return
	 */
	public ArrayList<String> capturaValoresCaratulaAcuse(String docSII) {

		ArrayList<String> out = new ArrayList<String>();

		try {

			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("Caratula");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RutResponde");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());// se agrega
																	// al
																	// arreglo
																	// out
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RutRecibe");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
			}
		} catch (Exception e) {
			// System.out.println("no es acuse");
			return out = new ArrayList<String>();
		}
		return out;
	}

	/**
	 * Obtiene los valores de RecepcionDTE de un archivo ACUSE POSICION ARRAY:
	 * 0-> TipoDTE 1-> Folio 2-> FchEmis 3-> RutEmisor 4-> RutRecep 5-> MntTotal
	 * 6-> EstadoRecepDTE 7-> RecepDTEGlosa
	 * 
	 * @param docSII
	 * @return
	 */
	public ArrayList<String> capturaValoresRecepcionDTEAcuse(String docSII) {
		// ERROR2
		ArrayList<String> out = new ArrayList<String>();

		try {

			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("RecepcionDTE");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TipoDTE");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("Folio");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("FchEmis");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTEmisor");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}

				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTRecep");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("MntTotal");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("EstadoRecepDTE");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RecepDTEGlosa");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		} catch (Exception e) {
			return out = new ArrayList<String>();
		}
		return out;
	}

	/**
	 * ACEPTACION COMERCIAL 0 TipoDTE 1 Folio 2 FchEmis 3 RUTEmisor 4 RUTRecep 5
	 * MntTotal 6 CodEnvio 7 EstadoDTE 8 EstadoDTEGlosa
	 * 
	 * @param docSII
	 * @return
	 */
	public ArrayList<String> capturaValoresResultadoDTE(String docSII) {
		ArrayList<String> out = new ArrayList<String>();

		try {

			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("ResultadoDTE");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TipoDTE");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {
					return out = new ArrayList<String>();

				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("Folio");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {
					return out = new ArrayList<String>();
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("FchEmis");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {

				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTEmisor");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {

				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTRecep");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {

				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("MntTotal");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {

				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("CodEnvio");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {

				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("EstadoDTE");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {

				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("EstadoDTEGlosa");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());// se
																		// agrega
																		// al
																		// arreglo
																		// out
					}
				} catch (Exception e) {

				}
			}
		} catch (Exception e) {

			return out = new ArrayList<String>();
		}
		return out;
	}

	/**
	 * Caputa los valores de un acuse de mercaderia
	 * 
	 * @param docSII
	 * @return
	 */
	public ArrayList<String> capturaValoresDocumentoRecibido(String docSII) {

		ArrayList<String> out = new ArrayList<String>();

		try {

			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("DocumentoRecibo");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TipoDoc");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());// se agrega
																	// al
																	// arreglo
																	// out
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("Folio");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("FchEmis");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());

				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTEmisor");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTRecep");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("MntTotal");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RutFirma");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("Declaracion");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TmstFirmaRecibo");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
			}
		} catch (Exception e) {
			return out = new ArrayList<String>();
		}
		return out;
	}

	/**
	 * Si es un intercambio devuelve un arrelo con los datos obtenidos de lo
	 * contrario devuelve un array
	 * 
	 * @param docSII
	 * @return
	 */
	public ArrayList<String> capturaValoresIntercambio(String docSII) {

		ArrayList<String> out = new ArrayList<String>();

		try {

			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("RecepcionDTE");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TipoDTE");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());// se agrega
																	// al
																	// arreglo
																	// out
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("Folio");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("FchEmis");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());

				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTEmisor");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTRecep");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("MntTotal");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("EstadoRecepDTE");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RecepDTEGlosa");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
			}
		} catch (Exception e) {
			return out = new ArrayList<String>();
		}
		return out;
	}

	/**
	 * Llena array con datos de documento Enviado por Tercerp si el documento
	 * corresponde con dicho formato de lo contrario entrega un Array con size()
	 * = 0
	 * 
	 * POSICION ARRAY: 0-> Rut Emisor 1-> RUT Envia(Rep Legal Tercero) 2-> RUT
	 * Receptor (Emp Actual) 3-> Fch Resolucion 4-> Nro Resolucion 5->
	 * TmstFirmaEnv
	 * 
	 * @param docSII
	 *            ruta donde se encuentran los documentos a analizar
	 * @return
	 */
	public ArrayList<String> capturaValoresCaratulaTercero(String docSII) {

		ArrayList<String> out = new ArrayList<String>();
		try {
			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("Caratula");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RutEmisor");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());// se agrega
																	// al
																	// arreglo
																	// out
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RutEnvia");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RutReceptor");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("FchResol");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("NroResol");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TmstFirmaEnv");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}

			}
		} catch (Exception e) {
			out = new ArrayList<String>();
		}
		return out;
	}

	public ArrayList<String> capturaValoresIdDocTercero(Node docSII) {

		ArrayList<String> out = new ArrayList<String>();

		try {
			Element fstElmnt1 = (Element) docSII;
			NodeList nodeLst = fstElmnt1.getElementsByTagName("IdDoc");
			

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TipoDTE");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());// se agrega
																	// al
																	// arreglo
																	// out
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("Folio");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("FchEmis");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}

			}
		} catch (Exception e) {
			out = new ArrayList<String>();
		}
		return out;
	}

	/**
	 * CAPTURA DATOS DE TAG EMISOR{0->RUTEmisor,}
	 * Se quitó [CiudadRecep] momentáneamente
	 * 
	 * @param docSII
	 * @return
	 */
	public ArrayList<String> capturaValoresEmisorTercero(Node docSII) {

		ArrayList<String> out = new ArrayList<String>();
		try {
			//File file = new File(docSII);
			Element fstElmnt1 = (Element) docSII;
			NodeList nodeLst = fstElmnt1.getElementsByTagName("Emisor");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
			

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTEmisor");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());// se agrega
																	// al
																	// arreglo
																	// out
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RznSoc");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("GiroEmis");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("Acteco");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("DirOrigen");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("CmnaOrigen");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("CiudadOrigen");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue());
					}

				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		} catch (Exception e) {
			out = new ArrayList<String>();
		}
		return out;
	}

	public ArrayList<String> capturaValoresReceptorTercero(Node docSII) {

		ArrayList<String> out = new ArrayList<String>();
		try {
			Element fstElmnt1 = (Element) docSII;
			NodeList nodeLst = fstElmnt1.getElementsByTagName("Receptor");
			

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RUTRecep");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());// se agrega
																	// al
																	// arreglo
																	// out
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("RznSocRecep");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("GiroRecep");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("DirRecep");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("CmnaRecep");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}

//				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
//					Element fstElmnt = (Element) fstNode;
//					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("CiudadRecep");
//					Element foliomElmnt = (Element) foliomElmntLst.item(0);
//					NodeList RENm = foliomElmnt.getChildNodes();
//					out.add(((Node) RENm.item(0)).getNodeValue());
//				}
			}
		} catch (Exception e) {
			out = new ArrayList<String>();
		}
		return out;
	}

	/**
	 * Totales de Terceros ArrayList POSICION:
	 * 0[MntNeto];1[MntExe];2[TasaIVA];3[IVA];4[MntTotal];
	 * 
	 * @param docSII
	 * @return
	 */
	public ArrayList<String> capturaValoresTotalesTercero(Node docSII) {

		ArrayList<String> out = new ArrayList<String>();
		try {
			Element fstElmnt1 = (Element) docSII;
			NodeList nodeLst = fstElmnt1.getElementsByTagName("Totales");
			

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("MntNeto");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue().replaceAll(" ", ""));// se
																							// agrega
																							// al
																							// arreglo
																							// out
					}
				} catch (Exception e) {
					out.add("");
				}

				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("MntExe");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue().replaceAll(" ", ""));
					}
				} catch (Exception e) {
					out.add("");
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("TasaIVA");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue().replaceAll(" ", ""));
					}
				} catch (Exception e) {
					out.add("");
				}
				try {
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) fstNode;
						NodeList foliomElmntLst = fstElmnt.getElementsByTagName("IVA");
						Element foliomElmnt = (Element) foliomElmntLst.item(0);
						NodeList RENm = foliomElmnt.getChildNodes();
						out.add(((Node) RENm.item(0)).getNodeValue().replaceAll(" ", ""));
					}
				} catch (Exception e) {
					out.add("");
				}
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("MntTotal");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue().replaceAll(" ", ""));
				}

			}
		} catch (Exception e) {
			out = new ArrayList<String>();
		}
		return out;
	}

	// GETTER AND SETTER
	public String getRutEmisor() {
		return rutEmisor;
	}

	public void setRutEmisor(String rutEmisor) {
		this.rutEmisor = rutEmisor;
	}

	public String getRutEnvia() {
		return rutEnvia;
	}

	public void setRutEnvia(String rutEnvia) {
		this.rutEnvia = rutEnvia;
	}

	public Long getTrackID() {
		return trackID;
	}

	public void setTrackID(Long trackID) {
		this.trackID = trackID;
	}

	public String getTmstRecepcion() {
		return tmstRecepcion;
	}

	public void setTmstRecepcion(String tmstRecepcion) {
		this.tmstRecepcion = tmstRecepcion;
	}

	public String getEstado1() {
		return estado1;
	}

	public void setEstado1(String estado1) {
		this.estado1 = estado1;
	}

	public Integer[][] getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(Integer[][] subTotal) {
		this.subTotal = subTotal;
	}

	public String[][] getRevisionDTE() {
		return revisionDTE;
	}

	public void setRevisionDTE(String[][] revisionDTE) {
		this.revisionDTE = revisionDTE;
	}

	public String[][] getDetalleRevisionDTE() {
		return detalleRevisionDTE;
	}

	public void setDetalleRevisionDTE(String[][] detalleRevisionDTE) {
		this.detalleRevisionDTE = detalleRevisionDTE;
	}

	public Integer getEre_id() {
		return ere_id;
	}

	public void setEre_id(Integer ere_id) {
		this.ere_id = ere_id;
	}

	public Boolean getIsReparo() {
		return isReparo;
	}

	public void setIsReparo(Boolean isReparo) {
		this.isReparo = isReparo;
	}

	public Boolean getIsRechazo() {
		return isRechazo;
	}

	public void setIsRechazo(Boolean isRechazo) {
		this.isRechazo = isRechazo;
	}

	
	public ArrayList<String> capturaValoresVarios(String docSII) {
		// TODO Auto-generated method stub
		ArrayList<String> out = new ArrayList<String>();

		try {
			File file = new File(docSII);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("DTE");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					NodeList foliomElmntLst = fstElmnt.getElementsByTagName("DTE");
					Element foliomElmnt = (Element) foliomElmntLst.item(0);
					NodeList RENm = foliomElmnt.getChildNodes();
					out.add(((Node) RENm.item(0)).getNodeValue());
				}
				
				
			}
			

		} catch (Exception e) {
			// TODO: handle exception
		}
		return out;
	}
}
