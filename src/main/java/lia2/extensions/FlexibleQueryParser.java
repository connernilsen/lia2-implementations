package lia2.extensions;

import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.SlopQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorPipeline;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.builders.SlopQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardQueryBuilder;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

public class FlexibleQueryParser extends StandardQueryParser {

  public FlexibleQueryParser(Analyzer analyzer) {
    super(analyzer);

    QueryNodeProcessorPipeline processors = (QueryNodeProcessorPipeline)
        this.getQueryNodeProcessor();
    processors.add(new NoFuzzyOrWildcardQueryProcessor());
    QueryTreeBuilder builders = (QueryTreeBuilder) this.getQueryBuilder();
    builders.setBuilder(TokenizedPhraseQueryNode.class,
        new SpanNearPhraseQueryBuilder());

    builders.setBuilder(SlopQueryNode.class, new SlopQueryNodeBuilder());
  }

  private static final class NoFuzzyOrWildcardQueryProcessor extends QueryNodeProcessorImpl {
    @Override
    protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
      if (node instanceof FuzzyQueryNode || node instanceof WildcardQueryNode) {
        throw new QueryNodeException(new MessageImpl("no"));
      }
      return node;
    }

    @Override
    protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
      return node;
    }

    @Override
    protected List<QueryNode> setChildrenOrder(List<QueryNode> children) {
      return children;
    }
  }

  private static class SpanNearPhraseQueryBuilder implements StandardQueryBuilder {
    @Override
    public Query build(QueryNode queryNode) throws QueryNodeException {
      TokenizedPhraseQueryNode phraseNode = (TokenizedPhraseQueryNode) queryNode;
      PhraseQuery phraseQuery = new PhraseQuery.Builder().build();
      List<QueryNode> children = phraseNode.getChildren();

      SpanTermQuery[] clauses;
      if (children != null) {
        int numTerms = children.size();
        clauses = new SpanTermQuery[numTerms];
        for (int i = 0; i < numTerms; i++) {
          FieldQueryNode termNode = (FieldQueryNode) children.get(i);
          TermQuery termQuery =
              (TermQuery) termNode.getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
          clauses[i] = new SpanTermQuery(termQuery.getTerm());
        }
      }
      else {
        clauses = new SpanTermQuery[0];
      }
      return new SpanNearQuery(clauses, phraseQuery.getSlop(), true);
    }
  }
}
