import { getMethodsByClass, getSession } from "@/lib/neo4j";
import { NextResponse } from "next/server";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const className = searchParams.get("className") ?? "";

    const session = getSession();
    const records = await getMethodsByClass(session, className);
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
