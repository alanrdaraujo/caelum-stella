package br.com.caelum.stella.validation;

import static br.com.caelum.stella.constraint.NITConstraints.NIT_FORMATED;
import static br.com.caelum.stella.constraint.NITConstraints.NIT_UNFORMATED;

import java.util.ArrayList;
import java.util.List;

import br.com.caelum.stella.MessageProducer;
import br.com.caelum.stella.SimpleMessageProducer;
import br.com.caelum.stella.format.NITFormatter;
import br.com.caelum.stella.validation.error.NITError;

/**
 * <p>
 * Validador do Número de Identificação do Trabalhador. Este documento contém 11
 * (onze) caracteres numéricos, no formato ddd.ddddd.dd-d.
 * </p>
 * <p>
 * O NIT corresponde ao número do <b>PIS/PASEP/CI</b> sendo que, no caso de
 * Contribuinte Individual (CI), pode ser utilizado o número de inscrição no
 * Sistema Único de Saúde (SUS) ou na Previdência Social.
 * </p>
 * 
 * @author Leonardo Bessa
 */
public class NITValidator extends BaseValidator<String> {

    private static final int MOD = 11;

    private final boolean isFormatted;

    private static class RotinaPosProdutoInterno implements
            RotinaDeDigitoVerificador {
        public Integer transform(RotinaParameters parameter) {
            Integer mod = parameter.getDigitoVerificadorInfo().getMod();
            Integer result = parameter.getResult() % mod;
            if (result < 2) {
                result = 0;
            } else {
                result = 11 - result;
            }
            return result;
        }
    }

    private static final Integer DV1_POSITION = 11;

    private static final Integer[] DV1_MULTIPLIERS = { 3, 2, 9, 8, 7, 6, 5, 4,
            3, 2 };

    private static final DigitoVerificadorInfo DV1_INFO = new DigitoVerificadorInfo(
            0,
            new RotinaDeDigitoVerificador[] { new RotinaPosProdutoInterno() },
            MOD, DV1_MULTIPLIERS, DV1_POSITION);

    private static final ValidadorDeDV DV1_CHECKER = new ValidadorDeDV(DV1_INFO);

    /**
     * Este considera, por padrão, que as cadeias estão formatadas e utiliza um
     * {@linkplain SimpleMessageProducer} para geração de mensagens.
     */
    public NITValidator() {
        this(true);
    }

    /**
     * O validador utiliza um {@linkplain SimpleMessageProducer} para geração de
     * mensagens.
     */
    public NITValidator(boolean isFormatted) {
        super();
        this.isFormatted = isFormatted;
    }

    /**
     * <p>
     * Construtor do Validador de NIT.
     * </p>
     * 
     * @param messageProducer
     *            produtor de mensagem de erro.
     * @param isFormatted
     *            considera cadeia no formato de NIT: "ddd.ddddd.dd-d" onde "d"
     *            é um dígito decimal.
     */
    public NITValidator(MessageProducer messageProducer, boolean isFormatted) {
        super(messageProducer);
        this.isFormatted = isFormatted;
    }

    protected List<InvalidValue> getInvalidValues(String nit) {
        List<InvalidValue> errors = new ArrayList<InvalidValue>();
        if (nit != null) {
            String unformatedNit = checkForCorrectFormat(nit, errors);

            if (errors.isEmpty()) {
                if (!hasValidCheckDigits(unformatedNit)) {
                    errors.add(NITError.INVALID_CHECK_DIGITS);
                }
            }
        }
        return errors;
    }

    private String checkForCorrectFormat(String string,
            List<InvalidValue> errors) {
        String unformatedNit;
        if (isFormatted) {
            if (!NIT_FORMATED.matcher(string).matches()) {
                errors.add(NITError.INVALID_FORMAT);
            }
            unformatedNit = (new NITFormatter()).unformat(string);
        } else {
            if (!NIT_UNFORMATED.matcher(string).matches()) {
                errors.add(NITError.INVALID_DIGITS);
            }
            unformatedNit = string;
        }
        return unformatedNit;
    }

    private boolean hasValidCheckDigits(String value) {
        return (DV1_CHECKER.isDVValid(value));
    }

    public boolean isEligible(String value) {
        boolean result;
        if (isFormatted) {
            result = NIT_FORMATED.matcher(value).matches();
        } else {
            result = NIT_FORMATED.matcher(value).matches();
        }
        return result;
    }
}
