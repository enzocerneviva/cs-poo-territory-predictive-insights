package dominio;

import excecao.FonteIndisponivelException;

/** Contrato de qualquer sensor do território. */
public interface Sensor {

    /** Lê o valor atual. Lança FonteIndisponivelException se o sensor falhar. */
    double lerValor() throws FonteIndisponivelException;

    boolean estaFuncionando();

    TipoSensor getTipo();
}
