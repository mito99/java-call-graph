import { getMethodsByClass, getSession } from "@/lib/neo4j";
import { NextResponse } from "next/server";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const packageName = searchParams.get("packageName") ?? "";
    const className = searchParams.get("className") ?? "";
    const methodName = searchParams.get("methodName") ?? "";

    const session = getSession();
    const records = await getMethodsByClass(
      session,
      packageName,
      className,
      methodName
    );
    await session.close();

    return NextResponse.json(records);
  } catch (error) {
    console.error("Error fetching data from Neo4j:", error);
    return NextResponse.json(
      { error: "Internal Server Error" },
      { status: 500 }
    );
  }
}
