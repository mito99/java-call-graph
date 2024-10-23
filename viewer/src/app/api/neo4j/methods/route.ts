import { getMethodsByClass, getSession } from "@/lib/neo4j";
import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const packageName = searchParams.get("packageName") ?? "";
    const className = searchParams.get("className") ?? "";
    const methodName = searchParams.get("methodName") ?? "";
    const limit = parseInt(searchParams.get("limit") ?? "25");
    const session = getSession();
    const records = await getMethodsByClass(session, {
      packageName,
      className,
      methodName,
      limit,
    });
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
