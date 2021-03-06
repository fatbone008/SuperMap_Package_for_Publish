//
//  SpatialRelationType.h
//  Visualization
//
//  版权所有 （c）2013 北京超图软件股份有限公司。保留所有权利。
//


/// 该类定义了数据集属性更新时的空间关系类型常量。
typedef  enum {
	/** 包含关系。
	*
	* 假设提供属性的数据集或记录集中的对象为 A，被更新的数据集或记录集中的对象为 B。若 A 包含 B，则对象B的属性将被对象 A 更新。  
	*/
    SRT_CONTION = 1,
	/** 被包含关系。
	*
	* 假设提供属性的数据集或记录集中的对象为 A，被更新的数据集或记录集中的对象为 B。若对象A 被对象 B 包含，则对象B 的属性将被对象 A 更新。   
	*/
    SRT_WITHIN  = 2,
	/**相交关系。
	*
	* 假设提供属性的数据集或记录集中的对象为 A，被更新的数据集或记录集中的对象为 B。若对象 B 与对象 A 相交，则对象B 的属性将被对象 A 更新。  
	*/
    SRT_INTERSERT = 3
}SpatialRelationType;
