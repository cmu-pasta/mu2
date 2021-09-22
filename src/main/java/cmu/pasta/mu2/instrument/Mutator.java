package cmu.pasta.mu2.instrument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Duplicates PIT's default mutator set
 *
 * @author Bella Laybourn
 */
public enum Mutator {
  //Math Ops (MATH) (note logic is bitwise, not to be confused with bool ops)
  I_ADD_TO_SUB(Opcodes.IADD, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.ISUB)),
  I_SUB_TO_ADD(Opcodes.ISUB, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.IADD)),
  I_MUL_TO_DIV(Opcodes.IMUL, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.IDIV)),
  I_DIV_TO_MUL(Opcodes.IDIV, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.IMUL)),
  I_REM_TO_MUL(Opcodes.IREM, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.IMUL)),
  I_OR_TO_AND(Opcodes.IOR, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.IAND)),
  I_AND_TO_OR(Opcodes.IAND, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.IOR)),
  I_XOR_TO_AND(Opcodes.IXOR, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.IAND)),
  I_SHL_TO_SHR(Opcodes.ISHL, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.ISHR)),
  I_SHR_TO_SHL(Opcodes.ISHR, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.ISHL)),
  I_USHR_TO_SHL(Opcodes.IUSHR, Type.INT_TYPE, Type.INT_TYPE, 2, true, null, new InstructionCall(Opcodes.ISHL)),
  L_ADD_TO_SUB(Opcodes.LADD, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LSUB)),
  L_SUB_TO_ADD(Opcodes.LSUB, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LADD)),
  L_MUL_TO_DIV(Opcodes.LMUL, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LDIV)),
  L_DIV_TO_MUL(Opcodes.LDIV, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LMUL)),
  L_REM_TO_MUL(Opcodes.LREM, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LMUL)),
  L_OR_TO_AND(Opcodes.LOR, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LAND)),
  L_AND_TO_OR(Opcodes.LAND, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LOR)),
  L_XOR_TO_AND(Opcodes.LXOR, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LAND)),
  L_SHL_TO_SHR(Opcodes.LSHL, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LSHR)),
  L_SHR_TO_SHL(Opcodes.LSHR, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LSHL)),
  L_USHR_TO_SHL(Opcodes.LUSHR, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.LSHL)),
  F_ADD_TO_SUB(Opcodes.FADD, Type.FLOAT_TYPE, Type.FLOAT_TYPE, 2, null, new InstructionCall(Opcodes.FSUB)),
  F_SUB_TO_ADD(Opcodes.FSUB, Type.FLOAT_TYPE, Type.FLOAT_TYPE, 2, null, new InstructionCall(Opcodes.FADD)),
  F_MUL_TO_DIV(Opcodes.FMUL, Type.FLOAT_TYPE, Type.FLOAT_TYPE, 2, null, new InstructionCall(Opcodes.FDIV)),
  F_DIV_TO_MUL(Opcodes.FDIV, Type.FLOAT_TYPE, Type.FLOAT_TYPE, 2, null, new InstructionCall(Opcodes.FMUL)),
  F_REM_TO_MUL(Opcodes.FREM, Type.FLOAT_TYPE, Type.FLOAT_TYPE, 2, null, new InstructionCall(Opcodes.FMUL)),
  D_ADD_TO_SUB(Opcodes.DADD, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, 2, null, new InstructionCall(Opcodes.DSUB)),
  D_SUB_TO_ADD(Opcodes.DSUB, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, 2, null, new InstructionCall(Opcodes.DADD)),
  D_MUL_TO_DIV(Opcodes.DMUL, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, 2, null, new InstructionCall(Opcodes.DDIV)),
  D_DIV_TO_MUL(Opcodes.DDIV, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, 2, null, new InstructionCall(Opcodes.DMUL)),
  D_REM_TO_MUL(Opcodes.DREM, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, 2, null, new InstructionCall(Opcodes.DMUL)),
  //Conditional Ops (CONDITIONALS_BOUNDARY):
  I_FLE_TO_FLT(Opcodes.IFLE, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFGE, null)),
  I_FGE_TO_FGT(Opcodes.IFGE, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFGT, null)),
  I_FGT_TO_FGE(Opcodes.IFGT, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFGE, null)),
  I_FLT_TO_FLE(Opcodes.IFLT, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFLE, null)),
  IF_ICM_PLE_TO_PLT(Opcodes.IF_ICMPLE, Type.INT_TYPE, Type.INT_TYPE, 2, null, new InstructionCall(Opcodes.IF_ICMPLT, null)),
  IF_ICM_PGE_TO_PGT(Opcodes.IF_ICMPGE, Type.INT_TYPE, Type.INT_TYPE, 2, null, new InstructionCall(Opcodes.IF_ICMPGT, null)),
  IF_ICM_PGT_TO_PGE(Opcodes.IF_ICMPGT, Type.INT_TYPE, Type.INT_TYPE, 2, null, new InstructionCall(Opcodes.IF_ICMPGE, null)),
  IF_ICM_PLT_TO_PLE(Opcodes.IF_ICMPLT, Type.INT_TYPE, Type.INT_TYPE, 2, null, new InstructionCall(Opcodes.IF_ICMPLE, null)),
  //Removing Negations (INVERT_NEGS):
  I_NEG_TO_NOP(Opcodes.INEG, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.NOP)),
  D_NEG_TO_NOP(Opcodes.DNEG, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, 2, null, new InstructionCall(Opcodes.NOP)),
  F_NEG_TO_NOP(Opcodes.FNEG, Type.FLOAT_TYPE, Type.FLOAT_TYPE, 2, null, new InstructionCall(Opcodes.NOP)),
  L_NEG_TO_NOP(Opcodes.LNEG, Type.LONG_TYPE, Type.LONG_TYPE, 2, null, new InstructionCall(Opcodes.NOP)),
  //Conditional Negation (NEGATE_CONDITIONALS):
  I_FEQ_TO_FNE(Opcodes.IFEQ, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFNE, null)),
  I_FNE_TO_FEQ(Opcodes.IFNE, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFEQ, null)),
  I_FLE_TO_FGT(Opcodes.IFLE, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFGT, null)),
  I_FGE_TO_FLT(Opcodes.IFGE, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFLT, null)),
  I_FGT_TO_FLE(Opcodes.IFGT, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFLE, null)),
  I_FLT_TO_FGE(Opcodes.IFLT, Type.INT_TYPE, Type.INT_TYPE, 1, null, new InstructionCall(Opcodes.IFGE, null)),
  IF_NULL_TO_NON(Opcodes.IFNULL, null, new InstructionCall(Opcodes.IFNONNULL, null)),
  IF_NON_TO_NULL(Opcodes.IFNONNULL, null, new InstructionCall(Opcodes.IFNULL, null)),
  IF_ICM_PNE_TO_PEQ(Opcodes.IF_ICMPNE, null, new InstructionCall(Opcodes.IF_ICMPEQ, null)),
  IF_ICM_PEQ_TO_PNE(Opcodes.IF_ICMPEQ, null, new InstructionCall(Opcodes.IF_ICMPNE, null)),
  IF_ICM_PLE_TO_PGT(Opcodes.IF_ICMPLE, null, new InstructionCall(Opcodes.IF_ICMPGT, null)),
  IF_ICM_PGE_TO_PLT(Opcodes.IF_ICMPGE, null, new InstructionCall(Opcodes.IF_ICMPLT, null)),
  IF_ICM_PGT_TO_PLE(Opcodes.IF_ICMPGT, null, new InstructionCall(Opcodes.IF_ICMPLE, null)),
  IF_ICM_PLT_TO_PGE(Opcodes.IF_ICMPLT, null, new InstructionCall(Opcodes.IF_ICMPGE, null)),
  IF_ACM_PEQ_TO_PNE(Opcodes.IF_ACMPEQ, null, new InstructionCall(Opcodes.IF_ACMPNE, null)),
  IF_ACM_PNE_TO_PEQ(Opcodes.IF_ACMPNE, null, new InstructionCall(Opcodes.IF_ACMPEQ, null)),
  //Boolean Replace Return (FALSE_RETURNS and TRUE_RETURNS):
  IRETURN_TO_FALSE(Opcodes.IRETURN, "Z", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0), new InstructionCall(Opcodes.IRETURN)),
  IRETURN_TO_TRUE(Opcodes.IRETURN, "Z", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_1), new InstructionCall(Opcodes.IRETURN)),
  ARETURN_TO_FALSE(Opcodes.ARETURN, "Ljava/lang/Boolean;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0),
      new InstructionCall(Opcodes.INVOKESTATIC, Boolean.class.getName().replace('.', '/'),
          "valueOf", "(Z)Ljava/lang/Boolean;", false),
      new InstructionCall(Opcodes.ARETURN)),
  ARETURN_TO_TRUE(Opcodes.ARETURN, "Ljava/lang/Boolean;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_1),
      new InstructionCall(Opcodes.INVOKESTATIC, Boolean.class.getName().replace('.', '/'),
          "valueOf", "(Z)Ljava/lang/Boolean;", false),
      new InstructionCall(Opcodes.ARETURN)),
  //Swap increments (INCREMENTS):
  IINC_SWAP(Opcodes.IINC, null, new InstructionCall(Opcodes.NOP)), //More symbolic
  //Return empty object (EMPTY_RETURNS)
  I_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/lang/Integer;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0),
      new InstructionCall(Opcodes.INVOKESTATIC, Integer.class.getName().replace('.', '/'),
          "valueOf", "(I)Ljava/lang/Integer;", false),
      new InstructionCall(Opcodes.ARETURN)),
  S_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/lang/Short;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0), //TODO check (seems okay, but make sure)
      new InstructionCall(Opcodes.INVOKESTATIC, Short.class.getName().replace('.', '/'), "valueOf",
          "(S)Ljava/lang/Short;", false),
      new InstructionCall(Opcodes.ARETURN)),
  C_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/lang/Character;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0), //TODO check (seems okay, but make sure)
      new InstructionCall(Opcodes.INVOKESTATIC, Character.class.getName().replace('.', '/'),
          "valueOf", "(C)Ljava/lang/Character;", false),
      new InstructionCall(Opcodes.ARETURN)),
  L_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/lang/Long;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.LCONST_0),
      new InstructionCall(Opcodes.INVOKESTATIC, Long.class.getName().replace('.', '/'), "valueOf",
          "(J)Ljava/lang/Long;", false),
      new InstructionCall(Opcodes.ARETURN)),
  F_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/lang/Float;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.FCONST_0),
      new InstructionCall(Opcodes.INVOKESTATIC, Float.class.getName().replace('.', '/'), "valueOf",
          "(F)Ljava/lang/Integer;", false),
      new InstructionCall(Opcodes.ARETURN)),
  D_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/lang/Double;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.DCONST_0),
      new InstructionCall(Opcodes.INVOKESTATIC, Double.class.getName().replace('.', '/'), "valueOf",
          "(D)Ljava/lang/Double;", false),
      new InstructionCall(Opcodes.ARETURN)),
  STR_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/lang/String;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.LDC, ""),
      new InstructionCall(Opcodes.ARETURN)),
  LST_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/util/List;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Collections", "emptyList",
          "()Ljava/util/List;", false),
      new InstructionCall(Opcodes.ARETURN)),
  SET_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/util/Set;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Collections", "emptySet",
          "()Ljava/util/Set;", false),
      new InstructionCall(Opcodes.ARETURN)),
  OPT_ARETURN_TO_EMPTY(Opcodes.ARETURN, "Ljava/util/Optional;", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.INVOKESTATIC, "java/util/Collections", "empty",
          "()Ljava/util/Optional;", false),
      new InstructionCall(Opcodes.ARETURN)),
  //Return null (NULL_RETURNS)
  ARETURN_TO_NULL(Opcodes.ARETURN, null, new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ACONST_NULL), new InstructionCall(Opcodes.ARETURN)),
  //Return 0 instead of primitive (PRIMITIVE_RETURNS)
  I_IRETURN_TO_0(Opcodes.IRETURN, "I", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0),
      new InstructionCall(Opcodes.IRETURN)),
  B_IRETURN_TO_0(Opcodes.IRETURN, "B", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0),
      new InstructionCall(Opcodes.IRETURN)),
  C_IRETURN_TO_0(Opcodes.IRETURN, "C", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0),
      new InstructionCall(Opcodes.IRETURN)),
  S_IRETURN_TO_0(Opcodes.IRETURN, "S", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.ICONST_0),
      new InstructionCall(Opcodes.IRETURN)),
  F_IRETURN_TO_0(Opcodes.IRETURN, "F", new InstructionCall(Opcodes.POP),
      new InstructionCall(Opcodes.FCONST_0),
      new InstructionCall(Opcodes.IRETURN)),
  L_IRETURN_TO_0(Opcodes.IRETURN, "J", new InstructionCall(Opcodes.POP2),
      new InstructionCall(Opcodes.LCONST_0),
      new InstructionCall(Opcodes.IRETURN)),
  D_IRETURN_TO_0(Opcodes.IRETURN, "D", new InstructionCall(Opcodes.POP2),
      new InstructionCall(Opcodes.LCONST_0),
      new InstructionCall(Opcodes.IRETURN)),
  ;

  private final int toReplace;
  private Type operandType;
  private Type typedReturnType;
  private int numArgs;
  private boolean infectionImplemented = false;
  private final String returnType;
  private final List<InstructionCall> replaceWith;
  public static int cvArg = Opcodes.ASM8;

  Mutator(int tR, String rT, InstructionCall... rw) {
    toReplace = tR;
    returnType = rT;
    replaceWith = new ArrayList<>();
    replaceWith.addAll(Arrays.asList(rw));
  }

  Mutator(int tR, Type operandType, Type returnType, int numArgs, String rT, InstructionCall... rw) {
    this(tR, rT, rw);
    this.operandType = operandType;
    this.typedReturnType = returnType;
    this.numArgs = numArgs;
  }

  Mutator(int tR, Type operandType, Type returnType, int numArgs, boolean infectionImplemented, String rT, InstructionCall... rw) {
    this(tR, rT, rw);
    this.operandType = operandType;
    this.typedReturnType = returnType;
    this.numArgs = numArgs;
    this.infectionImplemented = infectionImplemented;
  }

  public int toReplace() {
    return toReplace;
  }

  public boolean isOpportunity(int opcode, String descriptor) {
    return opcode == toReplace
        && (returnType == null || Type.getReturnType(descriptor).getDescriptor()
        .equals(returnType));
  }

  public List<InstructionCall> replaceWith() {
    return replaceWith;
  }

  public int getNumArgs() {
    return numArgs;
  }

  public Type getOperandType() {
    return operandType;
  }

  public Type getTypedReturnType() {
    return typedReturnType;
  }

  public boolean isInfectionImplemented() {
    return infectionImplemented;
  }

}
