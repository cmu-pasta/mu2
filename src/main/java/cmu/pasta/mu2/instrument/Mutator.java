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
  I_ADD_TO_SUB(Opcodes.IADD, null, new InstructionCall(Opcodes.ISUB)),
  I_SUB_TO_ADD(Opcodes.ISUB, null, new InstructionCall(Opcodes.IADD)),
  I_MUL_TO_DIV(Opcodes.IMUL, null, new InstructionCall(Opcodes.IDIV)),
  I_DIV_TO_MUL(Opcodes.IDIV, null, new InstructionCall(Opcodes.IMUL)),
  I_REM_TO_MUL(Opcodes.IREM, null, new InstructionCall(Opcodes.IMUL)),
  I_OR_TO_AND(Opcodes.IOR, null, new InstructionCall(Opcodes.IAND)),
  I_AND_TO_OR(Opcodes.IAND, null, new InstructionCall(Opcodes.IOR)),
  I_XOR_TO_AND(Opcodes.IXOR, null, new InstructionCall(Opcodes.IAND)),
  I_SHL_TO_SHR(Opcodes.ISHL, null, new InstructionCall(Opcodes.ISHR)),
  I_SHR_TO_SHL(Opcodes.ISHR, null, new InstructionCall(Opcodes.ISHL)),
  I_USHR_TO_SHL(Opcodes.IUSHR, null, new InstructionCall(Opcodes.ISHL)),
  L_ADD_TO_SUB(Opcodes.LADD, null, new InstructionCall(Opcodes.LSUB)),
  L_SUB_TO_ADD(Opcodes.LSUB, null, new InstructionCall(Opcodes.LADD)),
  L_MUL_TO_DIV(Opcodes.LMUL, null, new InstructionCall(Opcodes.LDIV)),
  L_DIV_TO_MUL(Opcodes.LDIV, null, new InstructionCall(Opcodes.LMUL)),
  L_REM_TO_MUL(Opcodes.LREM, null, new InstructionCall(Opcodes.LMUL)),
  L_OR_TO_AND(Opcodes.LOR, null, new InstructionCall(Opcodes.LAND)),
  L_AND_TO_OR(Opcodes.LAND, null, new InstructionCall(Opcodes.LOR)),
  L_XOR_TO_AND(Opcodes.LXOR, null, new InstructionCall(Opcodes.LAND)),
  L_SHL_TO_SHR(Opcodes.LSHL, null, new InstructionCall(Opcodes.LSHR)),
  L_SHR_TO_SHL(Opcodes.LSHR, null, new InstructionCall(Opcodes.LSHL)),
  L_USHR_TO_SHL(Opcodes.LUSHR, null, new InstructionCall(Opcodes.LSHL)),
  F_ADD_TO_SUB(Opcodes.FADD, null, new InstructionCall(Opcodes.FSUB)),
  F_SUB_TO_ADD(Opcodes.FSUB, null, new InstructionCall(Opcodes.FADD)),
  F_MUL_TO_DIV(Opcodes.FMUL, null, new InstructionCall(Opcodes.FDIV)),
  F_DIV_TO_MUL(Opcodes.FDIV, null, new InstructionCall(Opcodes.FMUL)),
  F_REM_TO_MUL(Opcodes.FREM, null, new InstructionCall(Opcodes.FMUL)),
  D_ADD_TO_SUB(Opcodes.DADD, null, new InstructionCall(Opcodes.DSUB)),
  D_SUB_TO_ADD(Opcodes.DSUB, null, new InstructionCall(Opcodes.DADD)),
  D_MUL_TO_DIV(Opcodes.DMUL, null, new InstructionCall(Opcodes.DDIV)),
  D_DIV_TO_MUL(Opcodes.DDIV, null, new InstructionCall(Opcodes.DMUL)),
  D_REM_TO_MUL(Opcodes.DREM, null, new InstructionCall(Opcodes.DMUL)),
  //Conditional Ops (CONDITIONALS_BOUNDARY):
  IF_LE_TO_LT(Opcodes.IFLE, null, new InstructionCall(Opcodes.IFGE, null)),
  IF_GE_TO_GT(Opcodes.IFGE, null, new InstructionCall(Opcodes.IFGT, null)),
  IF_GT_TO_GE(Opcodes.IFGT, null, new InstructionCall(Opcodes.IFGE, null)),
  IF_LT_TO_LE(Opcodes.IFLT, null, new InstructionCall(Opcodes.IFLE, null)),
  IF_ICMP_LE_TO_LT(Opcodes.IF_ICMPLE, null, new InstructionCall(Opcodes.IF_ICMPLT, null)),
  IF_ICMP_GE_TO_GT(Opcodes.IF_ICMPGE, null, new InstructionCall(Opcodes.IF_ICMPGT, null)),
  IF_ICMP_GT_TO_GE(Opcodes.IF_ICMPGT, null, new InstructionCall(Opcodes.IF_ICMPGE, null)),
  IF_ICMP_LT_TO_LE(Opcodes.IF_ICMPLT, null, new InstructionCall(Opcodes.IF_ICMPLE, null)),
  //Removing Negations (INVERT_NEGS):
  I_NEG_TO_NOP(Opcodes.INEG, null, new InstructionCall(Opcodes.NOP)),
  D_NEG_TO_NOP(Opcodes.DNEG, null, new InstructionCall(Opcodes.NOP)),
  F_NEG_TO_NOP(Opcodes.FNEG, null, new InstructionCall(Opcodes.NOP)),
  L_NEG_TO_NOP(Opcodes.LNEG, null, new InstructionCall(Opcodes.NOP)),
  //Conditional Negation (NEGATE_CONDITIONALS):
  IF_EQ_TO_NE(Opcodes.IFEQ, null, new InstructionCall(Opcodes.IFNE, null)),
  IF_NE_TO_EQ(Opcodes.IFNE, null, new InstructionCall(Opcodes.IFEQ, null)),
  IF_LE_TO_GT(Opcodes.IFLE, null, new InstructionCall(Opcodes.IFGT, null)),
  IF_GE_TO_LT(Opcodes.IFGE, null, new InstructionCall(Opcodes.IFLT, null)),
  IF_GT_TO_LE(Opcodes.IFGT, null, new InstructionCall(Opcodes.IFLE, null)),
  IF_LT_TO_GE(Opcodes.IFLT, null, new InstructionCall(Opcodes.IFGE, null)),
  IF_NULL_TO_NON(Opcodes.IFNULL, null, new InstructionCall(Opcodes.IFNONNULL, null)),
  IF_NON_TO_NULL(Opcodes.IFNONNULL, null, new InstructionCall(Opcodes.IFNULL, null)),
  IF_ICMP_NE_TO_EQ(Opcodes.IF_ICMPNE, null, new InstructionCall(Opcodes.IF_ICMPEQ, null)),
  IF_ICMP_EQ_TO_NE(Opcodes.IF_ICMPEQ, null, new InstructionCall(Opcodes.IF_ICMPNE, null)),
  IF_ICMP_LE_TO_GT(Opcodes.IF_ICMPLE, null, new InstructionCall(Opcodes.IF_ICMPGT, null)),
  IF_ICMP_GE_TO_LT(Opcodes.IF_ICMPGE, null, new InstructionCall(Opcodes.IF_ICMPLT, null)),
  IF_ICMP_GT_TO_LE(Opcodes.IF_ICMPGT, null, new InstructionCall(Opcodes.IF_ICMPLE, null)),
  IF_ICMP_LT_TO_GE(Opcodes.IF_ICMPLT, null, new InstructionCall(Opcodes.IF_ICMPGE, null)),
  IF_ACMP_EQ_TO_NE(Opcodes.IF_ACMPEQ, null, new InstructionCall(Opcodes.IF_ACMPNE, null)),
  IF_ACMP_NE_TO_EQ(Opcodes.IF_ACMPNE, null, new InstructionCall(Opcodes.IF_ACMPEQ, null)),
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
  private final String returnType;
  private final List<InstructionCall> replaceWith;
  public static int cvArg = Opcodes.ASM8;

  Mutator(int tR, String rT, InstructionCall... rw) {
    toReplace = tR;
    returnType = rT;
    replaceWith = new ArrayList<>();
    replaceWith.addAll(Arrays.asList(rw));
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

}
